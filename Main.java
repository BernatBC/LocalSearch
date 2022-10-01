import IA.Energia.*;
import aima.search.framework.GraphSearch;
import aima.search.framework.Problem;
import aima.search.framework.Search;
import aima.search.framework.SearchAgent;
import aima.search.informed.AStarSearch;
import aima.search.informed.IterativeDeepeningAStarSearch;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws Exception{

        Random rnd = new Random(78);

        int[] tipos_centrales = new int[] {1, 2, 3};
        int[] prop_clientes = new int[] {0.4, 0.4, 0.2}; // Ha de sumar 1.0

        Centrales centrales = new Centrales(tipos_centrales, rnd.nextInt());
        Clientes clientes = new Clientes(20, prop_clientes, 0.1, rnd.nextInt());

        //Inicialitzar assigacions buides
        int [] initial = new int [clientes.length];
        Arrays.fill(initial, -1);

        EnergyBoard board = new EnergyBoard(initial, clientes, centrales);

        //Inicialitzar solucions amb dues possibles estrategies
        board.initialState(rnd);
        //board.initialState2(rnd);

        // Create the Problem object
        Problem p = new  Problem(board,
                                new EnergySuccessorFunction(),
                                new EnergyGoalTest(),
                                new Energy5HeuristicFunction());

        // Instantiate the search algorithm
	// AStarSearch(new GraphSearch()) or IterativeDeepeningAStarSearch()
        Search alg = new AStarSearch(new GraphSearch());

        // Instantiate the SearchAgent object
        SearchAgent agent = new SearchAgent(p, alg);

	// We print the results of the search
        System.out.println();
        printActions(agent.getActions());
        printInstrumentation(agent.getInstrumentation());

        // You can access also to the goal state using the
	// method getGoalState of class Search

    }
        private static void printInstrumentation(Properties properties) {
        Iterator keys = properties.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            String property = properties.getProperty(key);
            System.out.println(key + " : " + property);
        }
        
    }
    
    private static void printActions(List actions) {
        for (int i = 0; i < actions.size(); i++) {
            String action = (String) actions.get(i);
            System.out.println(action);
        }
    }
    
}
