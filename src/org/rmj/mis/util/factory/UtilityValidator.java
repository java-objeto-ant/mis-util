package org.rmj.mis.util.factory;

import org.rmj.appdriver.agent.GRiderX;

public interface UtilityValidator {
    public void setGRider(GRiderX foValue);
    public boolean Run();
    
    public void setMessage(String fsValue);
    public String getMessage();
}
