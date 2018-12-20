package org.matsim.contrib.spatialDrt.firstLastAVPTRouter;


import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;

public class TransitRouterParams {
    final double marginalUtilityOfTravelTimePt_s;
    final double marginalUtilityOfTravelDistancePt_m;
    final double marginalUtilityWait_s;
    final double marginalUtilityAV_s;
    final double marginalUtilityAVTaxi_s;
    final double marginalUtilityWalk_s;
    final double marginalUtilityAV_m;
    final double marginalUtilityAVTaxi_m;
    final double marginalUtilityWalk_m;
    final double initialCostAVTaxi;
    final double initialCostAV;
    final double initialCostWalk;
    public TransitRouterParams(PlanCalcScoreConfigGroup pcsConfig){
        this.marginalUtilityOfTravelTimePt_s = pcsConfig.getModes().get("pt").getMarginalUtilityOfTraveling()/3600;
        this.marginalUtilityOfTravelDistancePt_m = pcsConfig.getModes().get("pt").getMarginalUtilityOfDistance();
        this.marginalUtilityWait_s = pcsConfig.getMarginalUtlOfWaitingPt_utils_hr()/3600;
        this.marginalUtilityAV_s = pcsConfig.getModes().get("drt").getMarginalUtilityOfTraveling()/3600;
        this.marginalUtilityAVTaxi_s = pcsConfig.getModes().get("drtaxi").getMarginalUtilityOfTraveling()/3600;
        this.marginalUtilityWalk_s = pcsConfig.getModes().get("walk").getMarginalUtilityOfTraveling()/3600;
        this.marginalUtilityAV_m = pcsConfig.getModes().get("drt").getMarginalUtilityOfDistance() + pcsConfig.getMarginalUtilityOfMoney() * pcsConfig.getModes().get("drt").getMonetaryDistanceRate();
        this.marginalUtilityAVTaxi_m = pcsConfig.getModes().get("drtaxi").getMarginalUtilityOfDistance() + pcsConfig.getMarginalUtilityOfMoney() * pcsConfig.getModes().get("drtaxi").getMonetaryDistanceRate();
        this.marginalUtilityWalk_m = pcsConfig.getModes().get("walk").getMarginalUtilityOfDistance() + pcsConfig.getMarginalUtilityOfMoney() * pcsConfig.getModes().get("walk").getMonetaryDistanceRate();
        this.initialCostAVTaxi = -pcsConfig.getModes().get("drtaxi").getConstant();
        this.initialCostAV = -pcsConfig.getModes().get("drt").getConstant();
        this.initialCostWalk = -pcsConfig.getModes().get("walk").getConstant();
    }
}

