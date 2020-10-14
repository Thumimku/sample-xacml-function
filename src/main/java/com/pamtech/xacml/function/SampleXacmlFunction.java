package com.pamtech.xacml.function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.balana.attr.AttributeValue;
import org.wso2.balana.attr.BagAttribute;
import org.wso2.balana.attr.StringAttribute;
import org.wso2.balana.cond.Evaluatable;
import org.wso2.balana.cond.EvaluationResult;
import org.wso2.balana.cond.FunctionBase;
import org.wso2.balana.ctx.EvaluationCtx;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * This is a custom function to return a bag strings that indicates the set of departments that the user is a member
 * of the given specific job level. This is retrieved based on the list of roles that the user is assigned to. The
 * roles may look like iam-engineer, iam-lead, iam-intern, apim-engineer, apim-lead, apim-intern.
 * When the roles and a job level is passed to this function, the corresponding list of departments is returned
 *
 * This function consumes two input values.
 *
 * 1. The list of roles (Bag of Strings)
 * 2. The job level (String)
 *
 * This function returns the list of departments (Bag of Strings)
 */
public class SampleXacmlFunction extends FunctionBase {

    public static final String FUNCTION_NAME = "string-custom-xacml-function";
    private static final int FUNCTION_ID = 0;
    private static final String params[] = {StringAttribute.identifier, StringAttribute.identifier};
    private static final boolean[] bagParams = new boolean[]{true, false};
    private static Log log = LogFactory.getLog(SampleXacmlFunction.class);

    public SampleXacmlFunction() {

        super(FUNCTION_NAME, FUNCTION_ID, params, bagParams, StringAttribute.identifier, true);
    }

    public static Set getSupportedIdentifiers() {

        Set set = new HashSet();
        set.add(FUNCTION_NAME);
        return set;
    }

    @Override
    public EvaluationResult evaluate(List inputs, EvaluationCtx context) {

        AttributeValue[] argValues = new AttributeValue[inputs.size()];
        EvaluationResult result = this.evalArgs(inputs, context, argValues);

        if (result != null) {
            return result;
        } else {
            BagAttribute bag = (BagAttribute) argValues[0];
            AttributeValue item = argValues[1];
            String jobLevelToBeChecked = ((StringAttribute) item).getValue();
            List<AttributeValue> filteredDepartmentList = new ArrayList<>();

            if (log.isDebugEnabled()) {
                log.debug("Received a bag of strings of size " + bag.size() + " containing the list of roles." +
                        "Departments with job level, " + jobLevelToBeChecked + " will be extracted from this.");
            }
            for (Iterator i = bag.iterator(); i.hasNext(); ) {
                StringAttribute roleAttribute = (StringAttribute) i.next();
                String role = roleAttribute.getValue();

                int index;
                // Checks whether the role name contains the given job level. If it has, retrieve the department name
                // from the role
                if ((index = role.indexOf(jobLevelToBeChecked)) >= 0) {

                    StringAttribute filteredDepartment = new StringAttribute(role.substring(0, index - 1));
                    filteredDepartmentList.add(filteredDepartment);
                }
            }
            try {
                BagAttribute departmentBag = new BagAttribute(new URI(StringAttribute.identifier), filteredDepartmentList);
                if (log.isDebugEnabled()) {
                    log.debug("Returning a bag of strings of size, " + departmentBag.size() + " containing the " +
                            "selected department names.");
                }
                return new EvaluationResult(departmentBag);
            } catch (URISyntaxException e) {
                log.error("Error occurred while creating the extracted department bag. ", e);
                return null;
            }
        }
    }
}
