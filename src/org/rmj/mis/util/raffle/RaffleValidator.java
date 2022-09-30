package org.rmj.mis.util.raffle;

import org.rmj.appdriver.agent.GRiderX;

public interface RaffleValidator {
    public void setGRider(GRiderX foValue);
    public void setBranch(String fsValue);
    public boolean Run();
    
    public void setMessage(String fsValue);
    public String getMessage();
}
