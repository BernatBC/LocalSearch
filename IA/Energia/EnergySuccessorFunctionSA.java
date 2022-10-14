package IA.Energia;

import aima.search.framework.SuccessorFunction;
import aima.search.framework.Successor;
import java.util.*;

public class EnergySuccessorFunctionSA implements SuccessorFunction
{
    public List getSuccessors(Object aState){
        ArrayList retVal = new ArrayList();
        EnergyBoard board = (EnergyBoard) aState;
        EnergyHeuristicFunction EHF = new EnergyHeuristicFunction();
    
        Random myRandom = new Random();
    
        if (myRandom.nextInt()%2 == 0) {
            //Swap
            int i = myRandom.nextInt(board.getNClients());
            int j;
            do {
                j = myRandom.nextInt(board.getNClients());
            } while (!board.canSwap(i, j));
    
            try {
                EnergyBoard newBoard = new EnergyBoard(board.getState(), board.getClientes(), board.getCentrales());

                newBoard.swap(i, j);

                double v = EHF.getHeuristicValue(newBoard);
                //String S = "SWAP " + i + " " + j + " HEUR "+v+" BEN " + newBoard.getBenefici() + newBoard.toString();
                String S = "SWAP " + i + " " + j + " HEUR "+v+" BEN " + newBoard.getBenefici() + newBoard.toString();
                System.out.println(S);
                retVal.add(new Successor(S, newBoard));

            } catch (Exception e){
                System.out.println(e);
            }    
        }
        else {
            //Assign
            int c = myRandom.nextInt(board.getNClients());
            int k;
    
            do {
                k = myRandom.nextInt(board.getNCentrals() + 1);
                //Dessasginar
                if (k == board.getNCentrals()) k = -1;
            } while (!board.canAssign(c, k));
    
            try {
                EnergyBoard newBoard = new EnergyBoard(board.getState(), board.getClientes(), board.getCentrales());
                newBoard.assign(c, k);
                double v = EHF.getHeuristicValue(newBoard);
                //String S = "ASSIGN CLIENT " + c + " to CENTRAL " + k + " HEUR "+v+" BEN " + newBoard.getBenefici() +  newBoard.toString();
                String S = "ASSIGN CLIENT " + c + " to CENTRAL " + k + " HEUR "+v+" BEN " + newBoard.getBenefici();
                System.out.println(S);

                retVal.add(new Successor(S, newBoard));

            } catch (Exception e){
                System.out.println(e);
            }    
        }
    
        return retVal;
    }
}

