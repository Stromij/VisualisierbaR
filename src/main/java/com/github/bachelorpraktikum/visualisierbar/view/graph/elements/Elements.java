package com.github.bachelorpraktikum.visualisierbar.view.graph.elements;

import com.github.bachelorpraktikum.visualisierbar.model.Element;
import com.github.bachelorpraktikum.visualisierbar.model.Node;
import com.github.bachelorpraktikum.visualisierbar.model.Switch;
import com.github.bachelorpraktikum.visualisierbar.view.graph.GraphShape;
import com.github.bachelorpraktikum.visualisierbar.view.graph.adapter.CoordinatesAdapter;

import java.util.*;

public final class Elements {
    private Elements() {
    }

    public static Collection<GraphShape<Element>> create(Node node, CoordinatesAdapter adapter) {
        List<GraphShape<Element>> shapes = new ArrayList<>(node.getElements().size());
        // TODO temporary hack to layout multiple elements //turns out this was not temporary
        int count = 0;

        List<Element> compositeElements = new LinkedList<>();
        Map<Switch, GraphShape<Element>> switches = new WeakHashMap<>();

        for (Element element : node.getElements()) {

            switch (element.getType()) {
                case Magnet:
                    if (element.getGraph()!=null) break;
                    shapes.add(new MagnetElement(element, node, adapter));
                    break;
                case SichtbarkeitsPunkt:
                case GefahrenPunkt:
                case Ne5:
                    if (element.getGraph()!=null) {count++; break;}
                    shapes.add(new RotatedDefaultOffsetElement(element, node, adapter, count++));
                    break;
                case WeichenPunkt:
                    if(element.getGraph()!=null) break;
                    Switch aSwitch = element.getSwitch();
                    if (!switches.containsKey(aSwitch)) {
                        GraphShape<Element> switchShape = new WeichenpunktElement(aSwitch, node,
                            adapter);
                        switches.put(aSwitch, switchShape);
                        shapes.add(switchShape);
                    }
                    break;
                case SwWechsel:
                    if (element.getGraph()!=null) break;
                    shapes.add(new StellwerkWechselElement(element, node, adapter));
                    break;
                case GeschwindigkeitsVoranzeiger:
                case VorSignal:
                case HauptSignal:
                case GeschwindigkeitsAnzeiger:
                case Zs10:
                    if (element.getGraph()!=null) break;
                    compositeElements.add(element);
                    break;
                default:
                    if (element.getGraph()!=null) {count++; break;}
                    shapes.add(new DummyElement(element, node, adapter, count++));
            }
        }
        if (!compositeElements.isEmpty()) {
            shapes.add(new CompositeElement(compositeElements, node, adapter, count));
        }
        return shapes;
    }
}
