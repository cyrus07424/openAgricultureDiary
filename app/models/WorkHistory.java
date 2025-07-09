package models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import play.data.format.Formats;
import play.data.validation.Constraints;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * WorkHistory (作業履歴) entity managed by Ebean
 */
@Entity
@Table(name = "work_history")
public class WorkHistory extends BaseModel {

    private static final long serialVersionUID = 1L;

    @Constraints.Required
    @Formats.DateTime(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @Constraints.Required
    private LocalTime startTime;

    @Constraints.Required
    private LocalTime endTime;

    @Constraints.Required
    @ManyToOne
    private Field field;

    @Constraints.Required
    @ManyToOne
    private Crop crop;

    @Constraints.Required
    @Constraints.MaxLength(1000)
    private String content;

    @ManyToOne
    private User user;

    public void update(WorkHistory newWorkHistoryData) {
        setDate(newWorkHistoryData.getDate());
        setStartTime(newWorkHistoryData.getStartTime());
        setEndTime(newWorkHistoryData.getEndTime());
        setField(newWorkHistoryData.getField());
        setCrop(newWorkHistoryData.getCrop());
        setContent(newWorkHistoryData.getContent());
        setUser(newWorkHistoryData.getUser());
        update();
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public Crop getCrop() {
        return crop;
    }

    public void setCrop(Crop crop) {
        this.crop = crop;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}