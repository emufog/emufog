/*
package emufog.fog;

import java.util.Comparator;

*/
/**
 * Custom comparator to sort a list of FogNodes.
 * The comparator uses two properties. First the comparator sorts descending
 * according to the average deployment costs and in case they are equal
 * the comparator sorts descending to the average connection costs.
 *//*

class FogComparator implements Comparator<FogNode> {

    @Override
    public int compare(FogNode fogNode, FogNode t1) {
        float cost1 = fogNode.getAverageDeploymentCosts();
        float cost2 = t1.getAverageDeploymentCosts();

        if (cost1 < cost2) {
            return -1;
        }

        if (cost2 < cost1) {
            return 1;
        }

        cost1 = fogNode.getAverageConnectionCosts();
        cost2 = t1.getAverageConnectionCosts();

        if (cost1 < cost2) {
            return -1;
        }

        if (cost2 < cost1) {
            return 1;
        }

        return 0;
    }
}
*/
