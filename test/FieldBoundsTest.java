import models.Field;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FieldBoundsTest {
    private Field field(double north, double east, double south, double west) {
        Field field = new Field();
        field.setNorthEastLatitude(north);
        field.setNorthEastLongitude(east);
        field.setSouthWestLatitude(south);
        field.setSouthWestLongitude(west);
        return field;
    }

    @Test
    public void acceptsValidBoundsInBothHemispheres() {
        assertTrue(field(36.1, 138.1, 36, 138).hasValidBounds());
        assertTrue(field(-35, -60, -36, -61).hasValidBounds());
        assertTrue(field(85.051129, 180, -85.051129, -180).hasValidBounds());
    }

    @Test
    public void rejectsMissingReversedDegenerateAndOutOfRangeBounds() {
        assertFalse(new Field().hasValidBounds());
        assertFalse(field(35, 138, 36, 139).hasValidBounds());
        assertFalse(field(36, 138, 36, 137).hasValidBounds());
        assertFalse(field(36, 138, 35, 138).hasValidBounds());
        assertFalse(field(86, 138, 35, 137).hasValidBounds());
        assertFalse(field(36, 181, 35, 137).hasValidBounds());
        assertFalse(field(36, 138, -86, 137).hasValidBounds());
        assertFalse(field(36, 138, 35, -181).hasValidBounds());
        assertFalse(field(Double.NaN, 138, 35, 137).hasValidBounds());
        assertFalse(field(36, Double.POSITIVE_INFINITY, 35, 137).hasValidBounds());
    }
}
