package IA.Energia;

import aima.search.framework.HeuristicFunction;

public class EnergyHeuristicFunction implements HeuristicFunction {
    
    public double getHeuristicValue(Object n){
        return ((EnergyBoard) n).getHeuristic();
    }
}