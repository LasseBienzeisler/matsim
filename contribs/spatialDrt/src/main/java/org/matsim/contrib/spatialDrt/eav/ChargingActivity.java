package org.matsim.contrib.spatialDrt.eav;

import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dynagent.AbstractDynActivity;
import org.matsim.contrib.spatialDrt.schedule.VehicleImpl;

public class ChargingActivity extends AbstractDynActivity {

    double endTime;
    VehicleImpl vehicle;
    Charger charger;

    public ChargingActivity(String activityType, DrtChargeTask task, Charger charger, Vehicle vehicle) {
        super(activityType);
        double chargingTime = charger.getChargingTime((VehicleImpl) vehicle);
        if (task.getBeginTime() + chargingTime > charger.getEndTime()){
            vehicle.getSchedule().getTasks().get(task.getTaskIdx() + 1).setBeginTime(charger.getEndTime());
        }
        this.endTime = Double.min(task.getBeginTime() + chargingTime, charger.getEndTime());
        task.setEndTime(endTime);
        this.vehicle = (VehicleImpl) vehicle;
        this.vehicle.changeStatus(false);
        this.charger = charger;
        //charger.addVehicle(vehicle, task.getEndTime());
    }

    @Override
    public void doSimStep(double now) {
        vehicle.charge(charger.getChargingRate());
    }

    @Override
    public double getEndTime() {
        return endTime;
    }
}
