package org.mvel.bsf;

import org.apache.bsf.util.BSFEngineImpl;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFDeclaredBean;
import org.mvel.MVEL;

import java.util.Map;
import java.util.HashMap;

public class MVELEngine extends BSFEngineImpl {
    private Map<String, Object> vars = new HashMap<String, Object>();

    public void declareBean(BSFDeclaredBean bsfDeclaredBean) throws BSFException {
        vars.put(bsfDeclaredBean.name, bsfDeclaredBean.bean);
    }

    public Object call(Object object, String string, Object[] objects) throws BSFException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object eval(String string, int i, int i1, Object object) throws BSFException {
        return MVEL.eval((String) object);
    }
}
