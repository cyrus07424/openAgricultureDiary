package forms;

import play.data.validation.Constraints;

/**
 * Form for pesticide data upload
 */
public class PesticideUploadForm {

    @Constraints.Required
    private String fileName;

    public PesticideUploadForm() {
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}