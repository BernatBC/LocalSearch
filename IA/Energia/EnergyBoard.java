package IA.Energia;
import java.lang.Math;

public class EnergyBoard {

    private int[] state;
    static private Clientes[] clientes;
    static private Centrales[] centrales;

    public EnergyBoard(int[] init, Clientes[] cli, Centrales[] centr){
        this.state = new int[init.length];
        this.clientes = cli.clone(); 

        for (int k = 0; k < init.length; ++k)
            this.state[k] = init[k]; 
    }

    public EnergyBoard getClone(){
        return (EnergyBoard) this.clone();
    }

    public void assign_ng(int c, int k){
        // Asignar la central k al cliente c 
        // PRE: El cliente c no tiene central si se va a asignar, o tiene si se va a desasignar 
        // PRE: El cliente es no garantizado
        state[c] = k;
    }

    public boolean canAssign_ng(int c, int k){
        if (clientes[c].getTipo() == Clientes.GARANTIZADO) return false;

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
}