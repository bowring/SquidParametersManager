/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cirdles.squidParametersManager.parameterModels;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.cirdles.squidParametersManager.ValueModel;
import org.cirdles.squidParametersManager.matrices.CorrelationMatrixModel;
import org.cirdles.squidParametersManager.matrices.CovarianceMatrixModel;
import org.cirdles.squidParametersManager.util.DateHelper;

/**
 *
 * @author ryanb
 */
public abstract class ParametersManager implements
        Comparable<ParametersManager>,
        Serializable {

    protected String modelName;
    protected String labName;
    protected String version;
    protected String dateCertified;
    protected String comments;
    protected String references;
    protected ValueModel[] values;
    protected CorrelationMatrixModel corrModel;
    protected CovarianceMatrixModel covModel;
    protected Map<String, BigDecimal> rhos;

    public ParametersManager() {
        modelName = "";
        labName = "";
        version = "";
        dateCertified = DateHelper.getCurrentDate();
        comments = "";
        references = "";
        values = new ValueModel[5];
        values[0] = new ValueModel("test", "test", "test", new BigDecimal(9.4983756873465873465687345),
                new BigDecimal(0));
        values[1] = new ValueModel("test", "test", "test", new BigDecimal(0),
                new BigDecimal(0));
        values[2] = new ValueModel("test", "test", "test", new BigDecimal(0),
                new BigDecimal(0));
        values[3] = new ValueModel("test", "test", "test", new BigDecimal(0),
                new BigDecimal(0));
        values[4] = new ValueModel("test", "test", "test", new BigDecimal(0),
                new BigDecimal(0));
        corrModel = new CorrelationMatrixModel();
        covModel = new CovarianceMatrixModel();
        this.rhos = new HashMap<>();
    }

    public ParametersManager(String modelName) {
        this.modelName = modelName;
        this.labName = labName;
        this.version = version;
        this.dateCertified = dateCertified;
        comments = "";
        references = "";
        values = new ValueModel[0];
        corrModel = new CorrelationMatrixModel();
        covModel = new CovarianceMatrixModel();
        this.rhos = new HashMap<>();
    }

    public ParametersManager(String modelName, String labName,
            String version, String dateCertified) {
        this.modelName = modelName;
        this.labName = labName;
        this.version = version;
        this.dateCertified = dateCertified;
        comments = "";
        references = "";
        values = new ValueModel[0];
        corrModel = new CorrelationMatrixModel();
        covModel = new CovarianceMatrixModel();
        this.rhos = new HashMap<>();
    }

    public ParametersManager(String modelName, String labName, String version,
            String dateCertified, String comments, String references) {
        this.modelName = modelName;
        this.labName = labName;
        this.version = version;
        this.dateCertified = dateCertified;
        this.comments = comments;
        this.references = references;
        values = new ValueModel[0];
        corrModel = new CorrelationMatrixModel();
        covModel = new CovarianceMatrixModel();
        this.rhos = new HashMap<>();
    }

    public ParametersManager(String modelName, String labName, String version,
            String dateCertified, String comments, String references,
            ValueModel[] values) {
        this.modelName = modelName;
        this.labName = labName;
        this.version = version;
        this.dateCertified = dateCertified;
        this.comments = comments;
        this.references = references;
        this.values = values;
        corrModel = new CorrelationMatrixModel();
        covModel = new CovarianceMatrixModel();
        this.rhos = new HashMap<>();
    }

    @Override
    public int compareTo(ParametersManager o) {
        return modelName.compareTo(o.getModelName());
    }

    public boolean equals(Object o) {
        boolean retVal = o instanceof ParametersManager;
        if (retVal && ((ParametersManager) o).
                getModelName().compareTo(modelName) != 0) {
            retVal = false;
        }
        return retVal;
    }

    public ValueModel getDatumByName(String datumName) {

        ValueModel retVal = new ValueModel(datumName);
        for (int i = 0; i < values.length; i++) {
            if (values[i].getName().equals(datumName)) {
                retVal = values[i];
            }
        }

        return retVal;
    }

    public void generateCorrelationsFromCovariances() {
        Iterator<String> colNames;
        try {
            corrModel.copyValuesFrom(covModel);
            // divide each cell by (1-sigma for x * 1-sigma for y)
            colNames = corrModel.getCols().keySet().iterator();
            while (colNames.hasNext()) {
                String colName = colNames.next();
                ValueModel colData = getDatumByName(colName);
                int col = corrModel.getCols().get(colName);
                //calculate values for this column
                int rowColDimension = corrModel.getMatrix().getColumnDimension();
                for (int row = 0; row < rowColDimension; row++) {
                    String rowName = corrModel.getRows().get(row);
                    ValueModel rowData = getDatumByName(rowName);
                    double correlation
                            = //
                            covModel.getMatrix().get(row, col)//
                            / rowData.getOneSigmaABS().doubleValue() //
                            / colData.getOneSigmaABS().doubleValue();
                    corrModel.setValueAt(row, col, correlation);
                }
            }
        } catch (Exception e) {
        }
    }

    public void generateCovariancesFromCorrelations() {
        Iterator<String> colNames;
        try {
            covModel.copyValuesFrom(corrModel);
            // divide each cell by (1-sigma for x * 1-sigma for y)
            colNames = covModel.getCols().keySet().iterator();
            while (colNames.hasNext()) {
                String colName = colNames.next();
                ValueModel colData = getDatumByName(colName);
                int col = covModel.getCols().get(colName);
                //calculate values for this column
                int rowColDimension = covModel.getMatrix().getColumnDimension();
                for (int row = 0; row < rowColDimension; row++) {
                    String rowName = covModel.getRows().get(row);
                    ValueModel rowData = getDatumByName(rowName);
                    double correlation
                            = //
                            corrModel.getMatrix().get(row, col)//
                            / rowData.getOneSigmaABS().doubleValue() //
                            / colData.getOneSigmaABS().doubleValue();
                    covModel.setValueAt(row, col, correlation);
                }
            }
        } catch (Exception e) {
        }
    }

    public abstract void initializeNewRatiosAndRhos();

    protected void buildRhosMap() {

        rhos = new HashMap<>();

        for (int i = 0; i < values.length; i++) {
            for (int j = i + 1; j < values.length; j++) {
                String key = "rho" + values[i].getName().substring(0, 1).toUpperCase()
                        + values[i].getName().substring(1) + "__" + values[j].getName();
                rhos.put(key, BigDecimal.ZERO);
            }
        }
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getReferences() {
        return references;
    }

    public void setReferences(String references) {
        this.references = references;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getLabName() {
        return labName;
    }

    public void setLabName(String labName) {
        this.labName = labName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDateCertified() {
        return dateCertified;
    }

    public void setDateCertified(String dateCertified) {
        this.dateCertified = dateCertified;
    }

    public ValueModel[] getValues() {
        return values;
    }

    public void setValues(ValueModel[] values) {
        this.values = values;
    }

    public CorrelationMatrixModel getCorrModel() {
        return corrModel;
    }

    public void setCorrModel(CorrelationMatrixModel corrModel) {
        this.corrModel = corrModel;
    }

    public CovarianceMatrixModel getCovModel() {
        return covModel;
    }

    public void setCovModel(CovarianceMatrixModel covModel) {
        this.covModel = covModel;
    }

    protected class DataValueModelNameComparator implements Comparator<ValueModel> {

        /**
         *
         */
        public DataValueModelNameComparator() {
        }

        @Override
        public int compare(ValueModel vm1, ValueModel vm2) {
            if (vm1.getName().substring(0, 1).equalsIgnoreCase(vm2.getName().substring(0, 1))) {
                return vm1.compareTo(vm2);
            } else {
                return vm2.compareTo(vm1);
            }
        }
    }

    public Map<String, BigDecimal> getRhos() {
        return rhos;
    }

    public void setRhos(Map<String, BigDecimal> rhos) {
        this.rhos = rhos;
    }

}
