import IA.Energia.Centrales;
import IA.Energia.Clientes;
import IA.Energia.EnergyBoard;
import IA.Energia.EnergyGoalTest;
import IA.Energia.EnergyHeuristicFunction;
import IA.Energia.EnergySuccessorFunction;
import aima.search.framework.GraphSearch;
import aima.search.framework.Problem;
import aima.search.framework.Search;
import aima.search.framework.SearchAgent;
import aima.search.informed.HillClimbingSearch;
import aima.search.informed.SimulatedAnnealingSearch;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception{

        Random rnd = new Random(43);

        int n_clientes = 8;
        int[] tipos_centrales = new int[] {2, 1, 1};
        double[] prop_clientes = new double[] {0.4, 0.4, 0.2}; // Ha de sumar 1.0

        Centrales centrales = new Centrales(tipos_centrales, rnd.nextInt());
        Clientes clientes = new Clientes(n_clientes, prop_clientes, 0.1, rnd.nextInt());

        //Inicialitzar assigacions buides
        int [] initial = new int [n_clientes];
        Arrays.fill(initial, -1);

        EnergyBoard board = new EnergyBoard(initial, clientes, centrales);

        //Inicialitzar solucions amb dues possibles estrategies
        System.out.println("----- ESTAT INICIAL ------");
        board.initialState(rnd);
        System.out.println(board);
        System.out.println("\n--- FI ESTAT INICIAL ---");

        // Create the Problem object
        Problem p = new  Problem(board,
                                new EnergySuccessorFunction(),
                                new EnergyGoalTest(),
                                new EnergyHeuristicFunction());

        //Search alg = new HillClimbingSearch();
        Search alg = new SimulatedAnnealingSearch();

        SearchAgent agent = new SearchAgent(p, alg);

	    // We print the results of the search
        printInstrumentation(agent.getInstrumentation());
        EnergyBoard end = (EnergyBoard) alg.getGoalState();

        System.out.println(end);
        System.out.println("HEURISTIC " + end.getHeuristic() + " BENEFIT "+ end.getBenefici());

    }

    private static void printInstrumentation(Properties properties) {
        Iterator keys = properties.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            String property = properties.getProperty(key);
            System.out.println(key + " : " + property);
        }
        
    }    
}
