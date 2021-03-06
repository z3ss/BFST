package ctrl;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import java.util.List;
import krakloader.NodeData;
import model.CurrentData;
import model.Road;
import view.Canvas;
import view.Graphics2DDraw;

/**
 * Implements: MouseListener, MouseMotionListener and MouseWheelListener, used
 * to zoom, pan and find nearest road.
 *
 * @author Group A
 */
public class ML implements MouseListener, MouseMotionListener, MouseWheelListener {

    private final Canvas c;
    private final CurrentData cd = CurrentData.getInstance();
    private final Graphics2DDraw j2d = null;
    private Point mouseStart;
    private Point mouseEnd;
    private Point currentMouse;
    private int mouseButton;
    private boolean mousePressed;
    private Rectangle2D currentView;
    private final Rectangle2D originalView;
    private long time = -501;

    /**
     * Constructor for ML, setting the current view and the original view. It
     * takes a Canvas 'c' as parameter, which it uses to calculate the scale.
     *
     */
    public ML() {
        currentView = new Rectangle2D.Double(cd.getXmin(), cd.getYmin(), cd.getXmax() - cd.getXmin(), cd.getYmax() - cd.getYmin());
        originalView = new Rectangle2D.Double(cd.getXmin(), cd.getYmin(), cd.getXmax() - cd.getXmin(), cd.getYmax() - cd.getYmin());
        c = Canvas.getInstance(cd);
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        //Do nothing
    }

    /**
     * Sets variables which define what mousebutton is pressed and that the
     * mousebutton is held down
     *
     * @param me
     */
    @Override
    public void mousePressed(MouseEvent me) {
        mouseStart = me.getPoint();
        mousePressed = true;
        mouseButton = me.getButton();
    }

    /**
     * Method to handle the MouseEvent occurring when a mouse button is
     * released. If it was the left mousebutton & it was dragged, it will zoom
     * on the selected area else if it wasn't dragged it will reset to the
     * original view. If it was any other button, nothing happens.
     *
     * @param me
     */
    @Override
    public void mouseReleased(MouseEvent me) {
        mouseEnd = me.getPoint();
        if (mouseButton == 3) {
            if (mouseStart.getX() == mouseEnd.getX() || mouseStart.getY() == mouseEnd.getY()) {
                cd.updateArea(originalView);
                currentView.setRect(originalView);
            } else {
                if (c.getScale() <= 1.1) {
                    cd.updateArea(new Rectangle2D.Double(cd.getOldx(), cd.getOldy(), c.getWidth(), c.getHeight()));
                } else {
                    double startx = mouseStart.getX();
                    double starty = mouseStart.getY();
                    double endx = mouseEnd.getX();
                    double endy = mouseEnd.getY();

                    double tmp;

                    if (startx > endx) {
                        tmp = startx;
                        startx = endx;
                        endx = tmp;
                    }

                    if (starty > endy) {
                        tmp = starty;
                        starty = endy;
                        endy = tmp;
                    }

                    double w = endx - startx;
                    double h = endy - starty;
                    if (w > h) {
                        double ratio = (double) c.getHeight() / (double) c.getWidth();
                        h = w * ratio;
                    } else {
                        double ratio = (double) c.getWidth() / (double) c.getHeight();
                        w = h * ratio;
                    }

                    currentView = new Rectangle2D.Double(startx, starty, w, h);
                    calcView(currentView);
                }
            }
        }
        c.setDragbool(false);
        mousePressed = false;
    }

    /**
     * Method to handle the MouseEvent occurring when the mouse is dragged. If
     * button pushed, while dragging is left mousebutton, it will draw the area
     * which is going to be zoomed in to. If is the right mousebutton, it will
     * pan the the map.
     *
     * @param e
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        currentMouse = e.getPoint();
        if (mouseButton == 3) {
            drawZoomArea();
        }

        if (mouseButton == 1) {
            pan();
        }

        e.consume();//Stops the event when not in use, makes program run faster
    }

    /**
     * when the mouse is moved, it will check for the nearest road.
     *
     * @param e
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        currentMouse = e.getPoint();
        getClosestRoad(e);
        e.consume();//Stops the event when not in use, makes program run faster

    }

    /*
     * Method to pan the view, which add the distance the mouse has moved to
     * the upper left corner, of the view.
     */
    private void pan() {
        Rectangle2D temp = cd.getView();
        double x = temp.getX() - ((currentMouse.getX() - mouseStart.getX()) * c.getScale());
        double y = temp.getY() - ((currentMouse.getY() - mouseStart.getY()) * c.getScale());
        double w = temp.getWidth();
        double h = temp.getHeight();
        cd.updateArea(new Rectangle2D.Double(x, y, w, h));
        mouseStart = currentMouse;
    }

    /* Method for finding the closest road, by distance to roadsegments.
     */
    private void getClosestRoad(MouseEvent e) {
        double eX, eY;
        eX = (e.getPoint().getX() * c.getScale()) + cd.getOldx();
        eY = (e.getPoint().getY() * c.getScale()) + cd.getOldy();
        Road closestRoad = null;

        List<Road> rl = CurrentData.getInstance().getTile(new Rectangle2D.Double(eX - 0.5, eY - 0.5, eX + 1, eY + 1));
        if (rl.size() > 0) {
            double dist = calcDist(rl.get(0), eX, eY);
            closestRoad = rl.get(0);
            for (Road road : rl) {
                if (closestRoad.getEd().VEJNAVN.isEmpty() && !road.getEd().VEJNAVN.isEmpty()) {
                    closestRoad = road;
                    dist = calcDist(road, eX, eY);
                    continue;
                }

                if (!road.getEd().VEJNAVN.isEmpty()) {
                    double temp = calcDist(road, eX, eY);;
                    if (temp < dist) {
                        closestRoad = road;
                        dist = temp;
                    }
                }
            }

            if (!closestRoad.getEd().VEJNAVN.isEmpty()) {

                CurrentData.setCurrentRoadLabel(closestRoad.getEd().VEJNAVN);
            } else {
                CurrentData.setCurrentRoadLabel("Vejen kunne ikke findes!");
            }

        }
    }
    
    /*
     * Calculates the shortest distance from the Road r to the point (eX, eY)
     */
    private double calcDist(Road r, double eX, double eY) {
        NodeData fN = r.getFn();
        NodeData tN = r.getTn();
        double v = Math.abs((tN.getX_COORD() - fN.getX_COORD()) * (fN.getY_COORD() - eY) - (fN.getX_COORD() - eX) * (tN.getY_COORD() - fN.getY_COORD()));
        double vR = Math.sqrt(Math.pow((tN.getX_COORD() - fN.getX_COORD()), 2) + Math.pow(tN.getY_COORD() - fN.getY_COORD(), 2));
        return v/vR;
    }

    /* Draws the rectangle which is zoomed in to.
     */
    private void drawZoomArea() {
        Graphics2D g = (Graphics2D) c.getGraphics();

        if (mousePressed) {
            double startx = mouseStart.getX();
            double starty = mouseStart.getY();
            double endx = currentMouse.getX();
            double endy = currentMouse.getY();

            double tmp;

            if (startx > endx) {
                tmp = startx;
                startx = endx;
                endx = tmp;
            }

            if (starty > endy) {
                tmp = starty;
                starty = endy;
                endy = tmp;
            }

            double w = endx - startx;
            double h = endy - starty;
            Rectangle2D rect = new Rectangle2D.Double(startx, starty, w, h);
            c.setDragrect(rect);    //Set up rectangle for nondisapearing draw
            c.setDragbool(true);
            c.repaint();

        }

    }

    /* Scales the rectangle from ML to fit with the coordinates in the Quadtree.
     */
    private void calcView(Rectangle2D r) {
        double x = r.getMinX() * c.getScale() + cd.getOldx();
        double y = r.getMinY() * c.getScale() + cd.getOldy();
        double w = r.getWidth() * c.getScale();
        double h = r.getHeight() * c.getScale();
        cd.updateArea(new Rectangle2D.Double(x, y, w, h));

    }

    @Override
    public void mouseEntered(MouseEvent me) {
        //does nothing
    }

    @Override
    public void mouseExited(MouseEvent me) {
        //does nothing
    }

    /**
     * Register if the mousewheel is moved and in which direction.
     *
     * @param e
     */
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if ((System.currentTimeMillis() - time) > 10) {
            if (e.getPreciseWheelRotation() < 0) {
                zoomIn(e.getX(), e.getY());
                time = System.currentTimeMillis();
            }
            if (e.getPreciseWheelRotation() > 0) {
                zoomOut(e.getX(), e.getY());
                time = System.currentTimeMillis();
            }
        }
    }

    /**
     * Zooms in 10% (width and height becomes 10% smaller) when mousewheel
     * scrolls up. New picture is dependant on mouse position.
     *
     * @param mouseX
     * @param mouseY
     */
    public void zoomIn(double mouseX, double mouseY) {
        double w = c.getWidth() * 0.9;
        double h = c.getHeight() * 0.9;
        double offx = (mouseX * 0.9) - mouseX;
        double offy = (mouseY * 0.9) - mouseY;
        calcView(new Rectangle2D.Double(-offx, -offy, w, h));
    }

    /**
     * Zooms out 10% (width and height becomes 10% bigger) when mousewheel
     * scrolls down. New picture is dependant on mouse position.
     *
     * @param mouseX X coordinate from mouse on component
     * @param mouseY Y coordinate from mouse on component
     */
    public void zoomOut(double mouseX, double mouseY) {
        double w = c.getWidth() * 1.1;
        double h = c.getHeight() * 1.1;
        double offx = (mouseX * 1.1) - mouseX;
        double offy = (mouseY * 1.1) - mouseY;
        calcView(new Rectangle2D.Double(-offx, -offy, w, h));
    }
}
