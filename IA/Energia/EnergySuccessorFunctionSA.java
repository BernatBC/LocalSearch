package IA.Energia;

import aima.search.framework.SuccessorFunction;
import aima.search.framework.Successor;
import java.util.*;

public class EnergySuccessorFunctionSA implements SuccessorFunction
{
    public List getSuccessors(Object aState){
        EnergyBoard board = (EnergyBoard) aState;
        EnergyHeuristicFunction EHF = new EnergyHeuristicFunction();
        Random myRandom = new Random();
    
        if (myRandom.nextInt()%2 == 0) return swap(board, EHF, myRandom);
        else return assign(board, EHF, myRandom);
    }

    private List swap(EnergyBoard board, EnergyHeuristicFunction EHF, Random myRandom) {
        ArrayList retVal = new ArrayList();
        int i = myRandom.nextInt(board.getNClients());
        int j;
        int timeout = board.getNClients();
        int timeout2 = board.getNClients();
        do {
            j = myRandom.nextInt(board.getNClients());
            if (--timeout <= 0) {
                timeout = board.getNClients();
                i = myRandom.nextInt(board.getNClients());
                if (--timeout2 <= 0) return assign(board, EHF, myRandom);
            }
        } while (!board.canSwap(i, j));
    
        try {
            EnergyBoard newBoard = new EnergyBoard(board.getState(), board.getClientes(), board.getCentrales());

            newBoard.swap(i, j);

            double v = EHF.getHeuristicValue(newBoard);
            //String S = "SWAP " + i + " " + j + " HEUR "+v+" BEN " + newBoard.getBenefici() + newBoard.toString();
            String S = "SWAP " + i + " " + j + " HEUR "+v+" BEN " + newBoard.getBenefici();
            //System.out.println(S);
            retVal.add(new Successor(S, newBoard));

        } catch (Exception e){
            System.out.println(e);
        }
        return retVal;    
    }

    private List assign(EnergyBoard board, EnergyHeuristicFunction EHF, Random myRandom) {
        ArrayList retVal = new ArrayList();
        int c = myRandom.nextInt(board.getNClients());
        int k;
        int timeout = board.getNCentrals();
        int timeout2 = board.getNClients();
        do {
            k = myRandom.nextInt(board.getNCentrals());
            //Dessasginar
            if (k == board.getNCentrals()) k = -1;
            if (--timeout <= 0) {
                timeout = board.getNCentrals();
                k = myRandom.nextInt(board.getNCentrals());
                if (--timeout2 <= 0) return swap(board, EHF, myRandom);
            }
        } while (!board.canAssign(c, k));
    
        try {
            EnergyBoard newBoard = new EnergyBoard(board.getState(), board.getClientes(), board.getCentrales());
            newBoard.assign(c, k);
            double v = EHF.getHeuristicValue(newBoard);
            //String S = "ASSIGN CLIENT " + c + " to CENTRAL " + k + " HEUR "+v+" BEN " + newBoard.getBenefici() +  newBoard.toString();
            String S = "ASSIGN CLIENT " + c + " to CENTRAL " + k + " HEUR "+v+" BEN " + newBoard.getBenefici();
            //System.out.println(S);

            retVal.add(new Successor(S, newBoard));

        } catch (Exception e){
            System.out.println(e);
        }    
        return retVal;
    }
}

