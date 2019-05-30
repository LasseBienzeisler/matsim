/* *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2018 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.contrib.spatialDrt.scheduler;

import com.google.inject.Inject;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.schedule.DrtTask;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.schedule.DriveTask;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedule.ScheduleStatus;
import org.matsim.contrib.dvrp.schedule.Schedules;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.contrib.dvrp.tracker.TaskTrackers;
import org.matsim.contrib.spatialDrt.eav.DrtChargeTask;
import org.matsim.contrib.spatialDrt.schedule.VehicleImpl;
import org.matsim.core.mobsim.framework.MobsimTimer;
import org.matsim.core.utils.misc.Time;

import java.util.List;

/**
 * @author michalm
 */
public class DrtScheduleTimingUpdater {
	private final double stopDuration;
	private final MobsimTimer timer;

	@Inject
	public DrtScheduleTimingUpdater(DrtConfigGroup drtCfg, MobsimTimer timer) {
		this.stopDuration = drtCfg.getStopDuration();
		this.timer = timer;
	}

	/**
	 * Check and decide if the schedule should be updated due to if vehicle is Update timings (i.e. beginTime and
	 * endTime) of all tasks in the schedule.
	 */
	public void updateBeforeNextTask(Vehicle vehicle) {
		Schedule schedule = vehicle.getSchedule();
		// Assumption: there is no delay as long as the schedule has not been started (PLANNED)
		if (schedule.getStatus() != ScheduleStatus.STARTED) {
			return;
		}

		updateTimingsStartingFromCurrentTask(vehicle, timer.getTimeOfDay());
	}

	public void updateTimings(Vehicle vehicle) {
		Schedule schedule = vehicle.getSchedule();
		if (schedule.getStatus() != ScheduleStatus.STARTED) {
			return;
		}

		double predictedEndTime = TaskTrackers.predictEndTime(schedule.getCurrentTask(), timer.getTimeOfDay());
		updateTimingsStartingFromCurrentTask(vehicle, predictedEndTime);
	}

	public void updateQueue(Vehicle vehicle){
		updateTimingsStartingFromTaskIdx(vehicle, vehicle.getSchedule().getCurrentTask().getTaskIdx() + 1, vehicle.getSchedule().getCurrentTask().getEndTime());
	}

	private void updateTimingsStartingFromCurrentTask(Vehicle vehicle, double newEndTime) {
		Schedule schedule = vehicle.getSchedule();
		Task currentTask = schedule.getCurrentTask();
		if (currentTask.getEndTime() != newEndTime) {
			currentTask.setEndTime(newEndTime);
			updateTimingsStartingFromTaskIdx(vehicle, currentTask.getTaskIdx() + 1, newEndTime);
		}
	}

	void updateTimingsStartingFromTaskIdx(Vehicle vehicle, int startIdx, double newBeginTime) {
		Schedule schedule = vehicle.getSchedule();
		List<? extends Task> tasks = schedule.getTasks();

		for (int i = startIdx; i < tasks.size(); i++) {
			DrtTask task = (DrtTask)tasks.get(i);
			double calcEndTime = calcNewEndTime(vehicle, task, newBeginTime);

			if (Time.isUndefinedTime(calcEndTime)) {
				schedule.removeTask(task);
				i--;
			} else if (calcEndTime < newBeginTime) {// 0 s is fine (e.g. last 'wait')
				throw new IllegalStateException();
			} else {
				task.setBeginTime(newBeginTime);
				task.setEndTime(calcEndTime);
				newBeginTime = calcEndTime;
			}
		}
	}

	private double calcNewEndTime(Vehicle vehicle, DrtTask task, double newBeginTime) {
		switch (task.getDrtTaskType()) {
			case STAY: {
				if (task instanceof DrtChargeTask){
					double duration = ((DrtChargeTask) task).getCharger().getChargingTime((VehicleImpl) vehicle);
					return newBeginTime + duration;
				}
				if (Schedules.getLastTask(vehicle.getSchedule()).equals(task)) {// last task
					// even if endTime=beginTime, do not remove this task!!! A DRT Schedule should end with WAIT
					return Math.max(newBeginTime, vehicle.getServiceEndTime());
				} else {
					// if this is not the last task then some other task (e.g. DRIVE or PICKUP)
					// must have been added at time submissionTime <= t
					double oldEndTime = task.getEndTime();
					if (oldEndTime <= newBeginTime) {// may happen if the previous task is delayed
						return Time.UNDEFINED_TIME;// remove the task
					} else {
						return oldEndTime;
					}
				}
			}

			case DRIVE: {
				// cannot be shortened/lengthen, therefore must be moved forward/backward
				VrpPathWithTravelData path = (VrpPathWithTravelData)((DriveTask)task).getPath();
				// TODO one may consider recalculation of SP!!!!
				return newBeginTime + path.getTravelTime();
			}

			case STOP: {
				// TODO does not consider prebooking!!!
				double duration = vehicle.getCapacity() * (((VehicleImpl)vehicle).getVehicleType().getAccessTime() + ((VehicleImpl)vehicle).getVehicleType().getEgressTime());
				return newBeginTime + duration;
			}

			default:
				throw new IllegalStateException();
		}
	}


}
