/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package FastestRoute;

import java.awt.geom.Point2D;
import model.Road;

/**
 *
 * @author Adam Engsig (adae@itu.dk)
 */
public class Linked
{
    private double length;
    private double drivetime;
    private Point2D.Double from; // Only null where the road search starts
    private DirectedEdge edge; // edge which contains the original road
    
    public Linked()
    {
        length = Double.POSITIVE_INFINITY;
        drivetime = Double.POSITIVE_INFINITY;
        from = null;
        edge = null;
    }

    public double getLength()
    {
        return length;
    }

    public Point2D.Double getFrom()
    {
        return from;
    }
    
    public void setFrom(Point2D.Double f)
    {
        from = f;
    }
    
    public void setLength(double l)
    {
        length = l;
    }
    
    public void setEdge(DirectedEdge e)
    {
        edge = e;
    }
    
    public void setDrivetime(double d)
    {
        drivetime = d;
    }
    
    public DirectedEdge getEdge()
    {
        return edge;
    }
    
    public Road getRoad()
    {
        return edge.getRoad();
    }
    
    public double getDrivetime()
    {
        return drivetime;
    }
}
