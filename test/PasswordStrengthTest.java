import org.junit.Test;
import static org.junit.Assert.*;
import services.PasswordStrengthService;

/**
 * Test for password strength validation
 */
public class PasswordStrengthTest {
    
    private PasswordStrengthService passwordStrengthService = new PasswordStrengthService();
    
    @Test
    public void testWeakPasswords() {
        // Test very weak passwords
        assertFalse("Empty password should be weak", passwordStrengthService.isPasswordStrong(""));
        assertFalse("Short password should be weak", passwordStrengthService.isPasswordStrong("123"));
        assertFalse("Common password should be weak", passwordStrengthService.isPasswordStrong("password"));
        assertFalse("Sequential password should be weak", passwordStrengthService.isPasswordStrong("123456"));
        assertFalse("Dictionary word should be weak", passwordStrengthService.isPasswordStrong("apple"));
    }
    
    @Test
    public void testStrongPasswords() {
        // Test strong passwords (should have score >= 2)
        assertTrue("Complex password should be strong", passwordStrengthService.isPasswordStrong("MyStr0ngP@ssw0rd!"));
        assertTrue("Long mixed password should be strong", passwordStrengthService.isPasswordStrong("TH1s_Is_A_V3ry_Str0ng_P@ssw0rd"));
        assertTrue("Random mixed password should be strong", passwordStrengthService.isPasswordStrong("Kj8#mN2$qR9"));
    }
    
    @Test
    public void testBorderlinePaswords() {
        // Test passwords that might be on the border
        String moderatePassword = "MyPassword123!";
        // This test is more about ensuring the service works than exact score
        assertNotNull("Service should return result for moderate password", 
                     passwordStrengthService.getPasswordStrength(moderatePassword));
    }
    
    @Test
    public void testNullAndEmptyPasswords() {
        assertFalse("Null password should be weak", passwordStrengthService.isPasswordStrong(null));
        assertFalse("Empty password should be weak", passwordStrengthService.isPasswordStrong(""));
        assertFalse("Whitespace password should be weak", passwordStrengthService.isPasswordStrong("   "));
        
        assertNotNull("Weakness message should be provided for null", 
                     passwordStrengthService.getWeaknessMessage(null));
        assertNotNull("Weakness message should be provided for empty", 
                     passwordStrengthService.getWeaknessMessage(""));
    }
    
    @Test
    public void testWeaknessMessages() {
        String weaknessMessage = passwordStrengthService.getWeaknessMessage("123");
        assertNotNull("Weakness message should not be null", weaknessMessage);
        assertTrue("Weakness message should be in Japanese", weaknessMessage.contains("パスワード"));
    }
}