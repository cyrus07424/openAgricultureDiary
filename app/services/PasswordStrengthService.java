package services;

import me.gosimple.nbvcxz.Nbvcxz;
import me.gosimple.nbvcxz.resources.Configuration;
import me.gosimple.nbvcxz.resources.ConfigurationBuilder;
import me.gosimple.nbvcxz.scoring.Result;

import javax.inject.Singleton;

/**
 * Service for password strength validation using nbvcxz library (zxcvbn port)
 */
@Singleton
public class PasswordStrengthService {
    
    private static final Nbvcxz nbvcxz;
    private static final int MINIMUM_STRENGTH_SCORE = 2; // Moderate strength required
    
    static {
        Configuration configuration = new ConfigurationBuilder()
                .setMinimumEntropy(40d)
                .createConfiguration();
        nbvcxz = new Nbvcxz(configuration);
    }
    
    /**
     * Validates password strength using nbvcxz (zxcvbn port)
     * 
     * @param password Password to validate
     * @return true if password is strong enough, false otherwise
     */
    public boolean isPasswordStrong(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        
        Result result = nbvcxz.estimate(password);
        return result.getBasicScore() >= MINIMUM_STRENGTH_SCORE;
    }
    
    /**
     * Gets password strength details for feedback
     * 
     * @param password Password to analyze
     * @return Result object with detailed analysis
     */
    public Result getPasswordStrength(String password) {
        if (password == null || password.trim().isEmpty()) {
            return null;
        }
        
        return nbvcxz.estimate(password);
    }
    
    /**
     * Gets Japanese error message for weak passwords
     * 
     * @param password Password that was analyzed
     * @return Japanese error message describing why password is weak
     */
    public String getWeaknessMessage(String password) {
        Result result = getPasswordStrength(password);
        if (result == null) {
            return "パスワードを入力してください";
        }
        
        switch (result.getBasicScore()) {
            case 0:
                return "パスワードが非常に弱いです。より複雑なパスワードを設定してください";
            case 1:
                return "パスワードが弱いです。文字の組み合わせや長さを改善してください";
            default:
                return "パスワードが弱いです。より安全なパスワードを設定してください";
        }
    }
}