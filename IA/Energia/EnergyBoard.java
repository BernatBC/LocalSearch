package IA.Energia;
import java.lang.Math;

public class EnergyBoard {

    private int[] state;
    static private Clientes clientes;
    static private Centrales centrales;

    /**
     * Construct a new EnergyBoard, given an optional initial state, the costumers and the power plants
     *
     * @exception ArithmeticException the length of the initial state array is different to the number of costumers.
     * @param init the initial state of the board. If init[i] = -1 that means costumer <i>i<i> won't have any power plant assigned.
     * @param cli the Clientes object which represents the array of costumers to serve.
     * @param centr the Centrales object which represents the array of power plants available to us.
     */
    public EnergyBoard(int[] init, Clientes cli, Centrales centr){

        if (init.length != cli.length) throw new ArithmeticException("The length of the state array should equal the number of costumers");
        
        this.state = new int[init.length];
        this.clientes = cli.clone();
        this.centrales = centr.clone(); 

        for (int k = 0; k < init.length; ++k)
            this.state[k] = init[k]; 
    }

    public EnergyBoard getClone(){
        return (EnergyBoard) this.clone();
    }


    /**
     * Assign (or deassign) a power plant to a costumer
     * 
     * This operation can only be (...) to be continued
     *
     */

    public void assign(int c, int k){
        // Asignar la central k (posiblemente k = -1 (quitar central) ) al cliente c 
        // PRE: El cliente c no tiene central si se va a asignar, o tiene si se va a desasignar 
        // PRE: El cliente es no garantizado
        state[c] = k;
    }

    public boolean canAssign(int c, int k){
        if (clientes[c].getTipo() == Clientes.GARANTIZADO) return false;

        if (state[c] != -1 && k == -1) return true;

        double energyleft = energyLeft(k);

        if (energyleft < clientes[c].getConsumo()*(1. + VEnergia.getPerdida(getDistance(c, k))))
            return false;
        else 
            return true;
    }

    public double energyLeft(int k){
        double eIni = centrales[k].getProduccion();
        
        for (int c = 0; c < clientes.length; ++c){
            if (state[c] != k) continue;

            eIni -= (clientes[c].getConsumo()*(1. + VEnergia.getPerdida(getDistance(c, k))));
        }

        return eIni;
    }

    public double getDistance(int c, int k){
        double cx = clientes[c].getCoordX();
        double cy = clientes[c].getCoordY();
        double kx = centrales[k].getCoordX();
        double ky = centrales[k].getCoordY();

        return Math.sqrt((cx-kx)*(cx-kx) + (cy-ky)*(cy-ky));
    }

    public boolean canSwap(int c1, int c2){
        if (state[c1] == state[c2]) return false;
        
        int k1 = state[c1]; 
        int k2 = state[c2];

        if (k1 == -1 && clientes[c2].getTipo() == Clientes.GARANTIZADO) return false;
        if (k2 == -1 && clientes[c1].getTipo() == Clientes.GARANTIZADO) return false;

        double energyleft1 = energyLeft(k1) - clientes[c1].getConsumo()*(1. + VEnergia.getPerdida(getDistance(c1, k1)));
        if (k1 != -1)
            if (energyleft1 < clientes[c2].getConsumo()*(1. + VEnergia.getPerdida(getDistance(c2, k1))))
                return false;

        double energyleft2 = energyLeft(k2) - clientes[c2].getConsumo()*(1. + VEnergia.getPerdida(getDistance(c2, k2)));
        if (k2 != -1)
            if (energyleft2 < clientes[c1].getConsumo()*(1. + VEnergia.getPerdida(getDistance(c1, k2))))
                return false;

        return true;
    }

    public void swap(int c1, int c2){
        // El client c1 passarà a tenir la central de c2 i el client c2 passarà a tenir la central del client c1
        // És possible que c1 o c2 no tinguin central, pero almenys una n'ha de tenir
        // central de c1 != central de c2,   s'han de seguir complint totes les restriccions de les solucions
        int k = state[c1];
        state[c1] = state[c2];
        state[c2] = k;
    }

    //Genera solució inicial on cada client garantit està assignat a una central de manera aleatòria. Els clients garantitzats no estan assignats tenen marcats central -1.
    public void initialState(Random rnd) {
        for (int i = 0; i < clientes.length; ++i) {
            if (clientes[i].getTipo() == Cliente.GARANTIZADO) state[i] = randomCentral(i, rnd);
        }
    }

    //Genera solució inicial on cada client està assignat a una central de manera aleatòria, els clients no garantitzats tenen possibilitats de no estar assignades a cap
    public void initialState2(Random rnd) {
        for (int i = 0; i < clientes.length; ++i) {
            if (clientes[i].getTipo() == Cliente.GARANTIZADO || rnd.nextInt()%2 == 0) state[i] = randomCentral(i, rnd);
        }
    }

    //Retorna una central aleatoria on el client es pot assignar
    private int randomCentral(int c, Random rnd) {
        int n_central = rnd.nextInt()%centrales.lenght;
        while (!canAssign(c, n_central)) {
            n_central = rnd.nextInt()%centrales.lenght;
        }
        assign(c, n_central);
    }
}