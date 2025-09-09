package models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import play.data.format.Formats;
import play.data.validation.Constraints;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * SoilDiagnostic (土壌診断) entity managed by Ebean
 */
@Entity
@Table(name = "soil_diagnostic")
public class SoilDiagnostic extends BaseModel {

    private static final long serialVersionUID = 1L;

    @Constraints.Required
    @Formats.DateTime(pattern = "yyyy-MM-dd")
    private LocalDate diagnosticDate;

    @Constraints.Required
    @ManyToOne
    private Field field;

    @ManyToOne
    private User user;

    // CEC (Cation Exchange Capacity)
    private BigDecimal cec;

    // EC (Electrical Conductivity)
    private BigDecimal ec;

    // pH H2O
    private BigDecimal phH2O;

    // pH KCL
    private BigDecimal phKCL;

    // アンモニア態窒素 NH4-N (Ammonium nitrogen)
    private BigDecimal nh4N;

    // カリ K2O (Potassium)
    private BigDecimal k2O;

    // リン酸吸収係数 (Phosphorus absorption coefficient)
    private BigDecimal phosphorusAbsorptionCoefficient;

    // 可給態窒素 (Available nitrogen)
    private BigDecimal availableNitrogen;

    // 有効態リン酸 P2O5 (Available phosphorus)
    private BigDecimal p2O5;

    // 石灰 CaO (Lime/Calcium oxide)
    private BigDecimal caO;

    // 硝酸態窒素 NO3-N (Nitrate nitrogen)
    private BigDecimal no3N;

    // 腐植 (Humus)
    private BigDecimal humus;

    // 苦土 MgO (Magnesium oxide)
    private BigDecimal mgO;

    public void update(SoilDiagnostic newSoilDiagnosticData) {
        setDiagnosticDate(newSoilDiagnosticData.getDiagnosticDate());
        setField(newSoilDiagnosticData.getField());
        setUser(newSoilDiagnosticData.getUser());
        setCec(newSoilDiagnosticData.getCec());
        setEc(newSoilDiagnosticData.getEc());
        setPhH2O(newSoilDiagnosticData.getPhH2O());
        setPhKCL(newSoilDiagnosticData.getPhKCL());
        setNh4N(newSoilDiagnosticData.getNh4N());
        setK2O(newSoilDiagnosticData.getK2O());
        setPhosphorusAbsorptionCoefficient(newSoilDiagnosticData.getPhosphorusAbsorptionCoefficient());
        setAvailableNitrogen(newSoilDiagnosticData.getAvailableNitrogen());
        setP2O5(newSoilDiagnosticData.getP2O5());
        setCaO(newSoilDiagnosticData.getCaO());
        setNo3N(newSoilDiagnosticData.getNo3N());
        setHumus(newSoilDiagnosticData.getHumus());
        setMgO(newSoilDiagnosticData.getMgO());
        update();
    }

    // Getters and Setters
    public LocalDate getDiagnosticDate() {
        return diagnosticDate;
    }

    public void setDiagnosticDate(LocalDate diagnosticDate) {
        this.diagnosticDate = diagnosticDate;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BigDecimal getCec() {
        return cec;
    }

    public void setCec(BigDecimal cec) {
        this.cec = cec;
    }

    public BigDecimal getEc() {
        return ec;
    }

    public void setEc(BigDecimal ec) {
        this.ec = ec;
    }

    public BigDecimal getPhH2O() {
        return phH2O;
    }

    public void setPhH2O(BigDecimal phH2O) {
        this.phH2O = phH2O;
    }

    public BigDecimal getPhKCL() {
        return phKCL;
    }

    public void setPhKCL(BigDecimal phKCL) {
        this.phKCL = phKCL;
    }

    public BigDecimal getNh4N() {
        return nh4N;
    }

    public void setNh4N(BigDecimal nh4N) {
        this.nh4N = nh4N;
    }

    public BigDecimal getK2O() {
        return k2O;
    }

    public void setK2O(BigDecimal k2O) {
        this.k2O = k2O;
    }

    public BigDecimal getPhosphorusAbsorptionCoefficient() {
        return phosphorusAbsorptionCoefficient;
    }

    public void setPhosphorusAbsorptionCoefficient(BigDecimal phosphorusAbsorptionCoefficient) {
        this.phosphorusAbsorptionCoefficient = phosphorusAbsorptionCoefficient;
    }

    public BigDecimal getAvailableNitrogen() {
        return availableNitrogen;
    }

    public void setAvailableNitrogen(BigDecimal availableNitrogen) {
        this.availableNitrogen = availableNitrogen;
    }

    public BigDecimal getP2O5() {
        return p2O5;
    }

    public void setP2O5(BigDecimal p2O5) {
        this.p2O5 = p2O5;
    }

    public BigDecimal getCaO() {
        return caO;
    }

    public void setCaO(BigDecimal caO) {
        this.caO = caO;
    }

    public BigDecimal getNo3N() {
        return no3N;
    }

    public void setNo3N(BigDecimal no3N) {
        this.no3N = no3N;
    }

    public BigDecimal getHumus() {
        return humus;
    }

    public void setHumus(BigDecimal humus) {
        this.humus = humus;
    }

    public BigDecimal getMgO() {
        return mgO;
    }

    public void setMgO(BigDecimal mgO) {
        this.mgO = mgO;
    }
}