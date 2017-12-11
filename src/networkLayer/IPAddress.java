/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networkLayer;

import java.io.Serializable;

/**
 *
 * @author samsung
 */
public class IPAddress implements Serializable{
    private Short bytes[] = new Short[4];
    private String str;

    public IPAddress(String str) {
        this.str = str;
        String[] temp = this.str.split("\\.");
        //System.out.println(temp.length);
        for(int i=0;i<4;i++)
        {
            bytes[i] = Short.parseShort(temp[i]);
        }
    }
    
    public Short[] getBytes()
    {
        return bytes;
    }
    
    public String getString()
    {
        return str;
    }

    @Override
    public String toString() {
        return str; //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int hashCode() {
        int value = 1;
        for(int i = 0; i < str.length(); i++){
            value = value * 37 + str.charAt(i);
        }
        return value;
    }

    public boolean arrayEqual(Short[] arr1, Short[] arr2){
        if(arr1.length != arr2.length){
            return false;
        }
        else{
            for(int i = 0; i < arr1.length; i++){
                if(!arr1[i].equals(arr2[i])){
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        IPAddress ip = (IPAddress) obj;
        if(arrayEqual(this.bytes, ip.bytes) && this.str.equals(ip.str)){
            return true;
        }
        return false;
    }
}
