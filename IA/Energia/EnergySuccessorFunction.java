package IA.Energia;

import aima.search.framework.SuccessorFunction;
import java.util.*;

public List getSuccessors(Object aState){
    ArrayList retVal = new ArrayList();
    EnergyBoard board = (EnergyBoard) aState;
    EnergyHeuristicFunction EHF = new EnergyHeuristicFunction();

    for (int i = 0; i < board.getNClients(); i++){
        for (int j = i + 1; j < board.getNClients(); j++){
            
            if (!board.canSwap(i, j)) continue;

            EnergyBoard newBoard = new EnergyBoard(board.getState(), board.getClientes(), board.getCentrales());

            newBoard.swap(i, j);

            int v = EHF.getHeuristicValue(newBoard);
            String S = "SWAP " + i + " " + j + " Coste("+v+")";

            retVal.add(new Successor(S, newBoard));
        } 
    }

    for (int i = 0; i < board.getNClients(); i++){
        if (board.canAssign(i, -1)){
            EnergyBoard newBoard = new EnergyBoard(board.getState(), board.getClientes(), board.getCentrales());
            newBoard.assign(i, -1);
            int v = EHF.getHeuristicValue(newBoard);
            String S = "DEASSIGN " + i + " Coste("+v+")";

            retVal.add(new Successor(S, newBoard));
        }

        for (int j = 0; j < board.getNCentrals(); ++j){
            if (board.canAssign(i, j)){
                EnergyBoard newBoard = new EnergyBoard(board.getState(), board.getClientes(), board.getCentrales());
                newBoard.assign(i, j);
                int v = EHF.getHeuristicValue(newBoard);
                String S = "ASSIGN " + i + " to " + j + " Coste("+v+")";

                retVal.add(new Successor(S, newBoard));
            }

        }
    }

    return retVal;
}