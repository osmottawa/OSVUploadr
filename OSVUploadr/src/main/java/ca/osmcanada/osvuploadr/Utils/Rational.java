/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.osmcanada.osvuploadr.Utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

/**
 *
 * @author james
 */
public class Rational {

    private int num, denom;

    static int[] reduceFraction(BigInteger num, BigInteger den) {
        int gcd = num.gcd(den).intValue(); 
        int[] rf = { num.divide(BigInteger.valueOf(gcd)).intValue(), den.divide(BigInteger.valueOf(gcd)).intValue() };
        return rf;
    }
    public Rational(double d) {
        String[] parts = String.valueOf(d).split("\\.");
        BigDecimal den=null;
        if(parts[1].length()>6){
            den=BigDecimal.TEN.pow(6);
            parts[1]=parts[1].substring(0, 6);
        }
        else{
            den=BigDecimal.TEN.pow(parts[1].length());
        }        
        BigDecimal num = (new BigDecimal(parts[0]).multiply(den)).add(new BigDecimal(parts[1])); // numerator
        int[] fraction = reduceFraction(num.toBigInteger(), den.toBigInteger());
        this.num = fraction[0]; this.denom = fraction[1];
    }

    public Rational(int num, int denom) {
        this.num = num; this.denom = denom;
    }

    Rational() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public int getNumerator(){
        return num;
    }
    
    public int getDenominator(){
        return denom;
    }
    public String toString() {
        return String.valueOf(num) + "/" + String.valueOf(denom);
    }
}

