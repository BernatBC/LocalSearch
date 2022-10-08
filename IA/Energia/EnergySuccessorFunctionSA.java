package IA.Energia;

import aima.search.framework.SuccessorFunction;
import java.util.*;

public List getSuccessors(Object aState){
    ArrayList retVal = new ArrayList();
    EnergyBoard board = (EnergyBoard) aState;
    EnergyHeuristicFunction EHF = new EnergyHeuristicFunction();

    Random myRandom = new Random();

    if (myRandom.nextInt()%2 == 0) {
        //Swap
        int c1 = myRandom.nextInt(board.getNClients());
        int c2;
        do {
            c2 = myRandom.nextInt(board.getNClients());
        } while (!board.canSwap(c1, c2));

        EnergyBoard newBoard = new EnergyBoard(board.getState(), board.getClientes(), board.getCentrales());

        newBoard.swap(i, j);

        int v = EHF.getHeuristicValue(newBoard);
        String S = "SWAP " + i + " " + j + " Coste("+v+")";

            retVal.add(new Successor(S, newBoard));
    }
    else {
        //Assign
        int c = myRandom.nextInt(board.getNClients());
        int k;

        do {
            k = myRandom.nextInt(board.getNCentrals() + 1);
            //Dessasginar
            if (k == board.getNCentrals) k = -1;
        } while (!board.canAssign(c, k));

        EnergyBoard newBoard = new EnergyBoard(board.getState(), board.getClientes(), board.getCentrales());
        newBoard.assign(c, k);
        int v = EHF.getHeuristicValue(newBoard);
        String S = "ASSIGN " + c + " to " + k + " Coste("+v+")";

        retVal.add(new Successor(S, newBoard));

    }

    return retVal;
}