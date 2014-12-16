package net.powermatcher.core.test;

import java.util.Date;

import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.PointBid;
import net.powermatcher.api.data.PricePoint;
import net.powermatcher.core.BaseDeviceAgent;

public class TestBaseDeviceAgent extends BaseDeviceAgent {

    @Override
    protected Date now() {
        return new Date();
    }
    
    public PointBid testCreateBid(PricePoint[] pricePoints){
        return super.createBid(pricePoints);
    }
    
    public MarketBasis testGetMarketBasis(){
        return super.getMarketBasis();
    }
    
    public int testGetCurrenBidNumber(){
        return super.getCurrentBidNr();
    }
    
    public void testSetAgentId(String agentId){
        super.setAgentId(agentId);
    }

}
