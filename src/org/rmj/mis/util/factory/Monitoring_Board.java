/**
 * Michael Torres Cuison
 * 
 * Mac 2021-09-28
 *      Started creating this object.
 */

package org.rmj.mis.util.factory;

import java.sql.SQLException;
import org.rmj.appdriver.agent.GRiderX;
import org.rmj.mis.util.sales.CPAccSalesTracker;
import org.rmj.mis.util.sales.CPUnitSalesTracker;
import org.rmj.mis.util.sales.MCSalesTracker;
import org.rmj.mis.util.sales.SPSalesTracker;

public class Monitoring_Board implements UtilityValidator{
    private GRiderX instance;
    private String sMessage;
    
    @Override
    public void setGRider(GRiderX foValue) {
        instance = foValue;
        
        if (instance == null){
            System.err.println("Application driver is not set.");
            System.exit(1);
        }
    }

    @Override
    public boolean Run() {
        if (instance == null){
            setMessage("Application driver is not set.");
            return false;
        }
        
        try {            
            MCSalesTracker loMCSales = new MCSalesTracker();
            loMCSales.setGRider(instance);
            loMCSales.setDivision("1");
            //loMCSales.setPeriod("202108");
            if (!loMCSales.CreateSource()){
                setMessage(loMCSales.getMessage());
                return false;
            }

            SPSalesTracker loSPSales = new SPSalesTracker();
            loSPSales.setGRider(instance);
            //loSPSales.setPeriod("202108");
            if (!loSPSales.CreateSource()){
                setMessage(loSPSales.getMessage());
                return false;
            }

            CPUnitSalesTracker loCPUnit = new CPUnitSalesTracker();
            loCPUnit.setGRider(instance);
            //loCPUnit.setPeriod("202108");
            if (!loCPUnit.CreateSource()){
                setMessage(loCPUnit.getMessage());
                return false;
            }

            CPAccSalesTracker loCPAcc = new CPAccSalesTracker();
            loCPAcc.setGRider(instance);
            //loCPAcc.setPeriod("202108");
            if (!loCPAcc.CreateSource()){
                setMessage(loCPAcc.getMessage());
                return false;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            setMessage(ex.getMessage());
            return false;
        }
        
        return true;
    }

    @Override
    public void setMessage(String fsValue) {
        sMessage = fsValue;
    }

    @Override
    public String getMessage() {
        return sMessage;
    }
}
