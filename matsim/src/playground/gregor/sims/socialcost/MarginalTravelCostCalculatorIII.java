package playground.gregor.sims.socialcost;

import org.matsim.core.api.network.Link;
import org.matsim.core.router.util.TravelCost;
import org.matsim.core.trafficmonitoring.AbstractTravelTimeCalculator;
import org.matsim.core.utils.misc.IntegerCache;

public class MarginalTravelCostCalculatorIII implements TravelCost {
	
	
	private final SocialCostCalculator sc;
	private final AbstractTravelTimeCalculator tc;
	private int binSize;

	public MarginalTravelCostCalculatorIII(final AbstractTravelTimeCalculator tc, final SocialCostCalculator sc, final int binSize) {
		this.tc = tc;
		this.sc = sc;
		this.binSize = binSize;
	}

	public double getLinkTravelCost(final Link link, double time) {
		double t2, t1;
		Integer k1;
		double diff = time % this.binSize;
		if (diff > this.binSize/2) {
			k1 = getTimeBin(time);
		} else {
			k1 = getTimeBin(time - this.binSize);
		}

		t1 = k1 * this.binSize + this.binSize/2;
//		k2 = k1 +1;
		t2 = t1 + this.binSize;
		double w = (t2 - time) / (double)this.binSize;
		
		double t = w * this.tc.getLinkTravelTime(link, t1) + (1-w) * this.tc.getLinkTravelTime(link, t2);
		double s = w * this.sc.getSocialCost(link, t1) + (1-w) * this.sc.getSocialCost(link, t2);
		double cost = t+s;
//		if (cost < 0) {
//			System.err.println("negative cost:" + cost);
//		} else if (Double.isNaN(cost)) {
//			System.err.println("nan cost:" + cost);
//		} else if (Double.isInfinite(cost)) {
//			System.err.println("infinite cost:" + cost);
//		} else if (cost > 10000) {
//			System.out.println("verry high cost:" + cost);
//		}
		return cost;
	}
	private Integer getTimeBin(double time) {
		int slice = ((int) time)/this.binSize;
		return IntegerCache.getInteger(slice);
	}
	
}
