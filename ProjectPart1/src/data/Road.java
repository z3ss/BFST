/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package data;

import krakloader.EdgeData;
import krakloader.NodeData;

/**
 *
 * @author KristianMohr
 */
public class Road {
    private EdgeData ed;
    private double midX, midY;
    
    public Road(EdgeData ed, NodeData fn, NodeData tn){
        this.ed = ed;
        midX = (fn.X_COORD+tn.X_COORD)/2;
        midY = (fn.Y_COORD+tn.Y_COORD)/2;
    }
}