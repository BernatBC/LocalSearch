package IA.Energia;

import aima.search.framework.SuccessorFunction;
import java.util.*;
import aima.search.framework.Successor;

public class EnergySuccessorFunction implements SuccessorFunction{

    public List getSuccessors(Object aState) {
        ArrayList retVal = new ArrayList();
        EnergyBoard board = (EnergyBoard) aState;
        EnergyHeuristicFunction EHF = new EnergyHeuristicFunction();


        for (int i = 0; i < board.getNClients(); i++){
            for (int j = i + 1; j < board.getNClients(); j++){
                
                if (!board.canSwap(i, j)) continue;
    
                try {
					// EnergyBoard(int[] init, double ben, double dis, double ene, double[] eleft, int[] cXk){
                    EnergyBoard newBoard = new EnergyBoard(board.getState(), board.getBenefici(), board.getDistance(),
															board.getEnergy(), board.getEnergyLeft(), board.getcXk(), board.getNAG());

                    newBoard.swap(i, j);
    
                    double v = EHF.getHeuristicValue(newBoard);
                    //String S = "SWAP " + i + " " + j + " HEUR "+v+" BEN " + newBoard.getBenefici() + newBoard.toString();
                    String S = "SWAP " + i + " " + j + " HEUR "+v+" BEN " + newBoard.getBenefici();

                    System.out.println(S);

                    retVal.add(new Successor(S, newBoard));
    
                } catch (Exception e){
                    System.out.println(e);
                }    
            } 
        }

        for (int i = 0; i < board.getNClients(); i++){
            /*if (board.canAssign(i, -1)){
                try {
                    EnergyBoard newBoard = new EnergyBoard(board.getState(), board.getBenefici(), board.getDistance(),
															board.getEnergy(), board.getEnergyLeft(), board.getcXk(), board.getNAG());
                    newBoard.assign(i, -1);
                    double v = EHF.getHeuristicValue(newBoard);
                    //String S = "DEASSIGN " + i + " HEUR "+v+" BEN " + newBoard.getBenefici() + newBoard.toString();
                    String S = "DEASSIGN " + i + " HEUR "+v+" BEN " + newBoard.getBenefici();
                    //System.out.println(S);

                    retVal.add(new Successor(S, newBoard));
    
                } catch (Exception e){
                    System.out.println(e);
                }
            }*/
    
            for (int j = 0; j < board.getNCentrals(); ++j){
                if (board.canAssign(i, j)){
                    try {
						EnergyBoard newBoard = new EnergyBoard(board.getState(), board.getBenefici(), board.getDistance(),
															board.getEnergy(), board.getEnergyLeft(), board.getcXk(), board.getNAG());
                        newBoard.assign(i, j);
                        double v = EHF.getHeuristicValue(newBoard);
                        //String S = "ASSIGN CLIENT " + i + " to CENTRAL " + j + " HEUR "+v+" BEN " + newBoard.getBenefici() +  newBoard.toString();
                        String S = "ASSIGN CLIENT " + i + " to CENTRAL " + j + " HEUR "+v+" BEN " + newBoard.getBenefici();

                        System.out.println(S);

                        retVal.add(new Successor(S, newBoard));
    
                    } catch (Exception e){
                        System.out.println(e);
                    }
                }
    
            }
        }
    
        return retVal;
    }
}
