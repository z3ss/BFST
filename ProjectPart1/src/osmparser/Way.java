/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package osmparser;

/**
 *
 * @author z3ss
 */
public class Way {
    private long id;
    public Way(long id) {
        this.id = id;
    }
    
    public void addNode(long nID){
        
    } 
    
    public long getID(){
        return id;
    }
}
