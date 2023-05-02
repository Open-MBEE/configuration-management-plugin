package org.openmbee.plugin.cfgmgmt.integration.threedx.json;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ThreeDxQueryJson {
    @SerializedName(value ="with_indexing_date")
    private boolean withIndexingDate;
    @SerializedName(value ="with_synthesis")
    private boolean withSynthesis;
    @SerializedName(value ="with_nls")
    private boolean withNls;
    private String label;
    private String locale;
    @SerializedName(value ="select_predicate")
    private List<String> selectPredicate;
    @SerializedName(value ="select_file")
    private List<String> selectFile;
    private String query;
    private JsonElement refine;
    @SerializedName(value ="order_by")
    private String orderBy;
    @SerializedName(value ="order_field")
    private String orderField;
    @SerializedName(value ="select_snippets")
    private List<String> selectSnippets;
    private int nresults;
    private String start;
    @SerializedName(value ="next_start")
    private String nextStart;
    private List<String> source;
    private String tenant;

    public ThreeDxQueryJson(boolean withIndexingDate, boolean withSynthesis, boolean withNls, String label, String locale,
                            List<String> selectPredicate, List<String> selectFile, String query, JsonElement refine,
                            String orderBy, String orderField, List<String> selectSnippets, int nresults, String start,
                            String nextStart, List<String> source, String tenant) {
        this.withIndexingDate = withIndexingDate;
        this.withSynthesis = withSynthesis;
        this.withNls = withNls;
        this.label = label;
        this.locale = locale;
        this.selectPredicate = selectPredicate;
        this.selectFile = selectFile;
        this.query = query;
        this.refine = refine;
        this.orderBy = orderBy;
        this.orderField = orderField;
        this.selectSnippets = selectSnippets;
        this.nresults = nresults;
        this.start = start;
        this.nextStart = nextStart;
        this.source = source;
        this.tenant = tenant;
    }

    public boolean isWithIndexingDate() {
        return withIndexingDate;
    }

    public void setWithIndexingDate(boolean withIndexingDate) {
        this.withIndexingDate = withIndexingDate;
    }

    public boolean isWithSynthesis() {
        return withSynthesis;
    }

    public void setWithSynthesis(boolean withSynthesis) {
        this.withSynthesis = withSynthesis;
    }

    public boolean isWithNls() {
        return withNls;
    }

    public void setWithNls(boolean withNls) {
        this.withNls = withNls;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public List<String> getSelectPredicate() {
        return selectPredicate;
    }

    public void setSelectPredicate(List<String> selectPredicate) {
        this.selectPredicate = selectPredicate;
    }

    public List<String> getSelectFile() {
        return selectFile;
    }

    public void setSelectFile(List<String> selectFile) {
        this.selectFile = selectFile;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public JsonElement getRefine() {
        return refine;
    }

    public void setRefine(JsonElement refine) {
        this.refine = refine;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getOrderField() {
        return orderField;
    }

    public void setOrderField(String orderField) {
        this.orderField = orderField;
    }

    public List<String> getSelectSnippets() {
        return selectSnippets;
    }

    public void setSelectSnippets(List<String> selectSnippets) {
        this.selectSnippets = selectSnippets;
    }

    public int getNresults() {
        return nresults;
    }

    public void setNresults(int nresults) {
        this.nresults = nresults;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getNextStart() {
        return nextStart;
    }

    public void setNextStart(String nextStart) {
        this.nextStart = nextStart;
    }

    public List<String> getSource() {
        return source;
    }

    public void setSource(List<String> source) {
        this.source = source;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }
}
