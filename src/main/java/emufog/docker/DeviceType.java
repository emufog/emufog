/*
package emufog.docker;

*/
/**
 * This docker image represents a host device connected to an edge router.
 * By using the scaling factor you can simulate a higher load of multiple devices.
 *//*

public class DeviceType extends DockerType {

    */
/* scaling factor of the docker image to simulate multiple hosts *//*

    public final int scalingFactor;

    */
/* average devices connected to an edge router *//*

    public final float averageDeviceCount;

    */
/**
     * Creates a new device docker instance based on the abstract DockerType.
     * Can be used to simulate multiple devices by the scaling factor.
     * Will be distributed based on the average count per router.
     *
     * @param dockerImage        actual docker image to deploy
     * @param scalingFactor      scaling factor of this docker image, factor >= 1
     * @param averageDeviceCount average number of devices of this image deployed to each router
     * @param memoryLimit        upper limit of memory to use in Bytes
     * @param cpuShare           of the sum of available computing resources
     * @throws IllegalArgumentException the docker image name cannot be null and must
     *                                  match the pattern of a docker container name
     *//*

    public DeviceType(*/
/*String dockerImage,*//*
 int scalingFactor, float averageDeviceCount, int memoryLimit, float cpuShare) throws IllegalArgumentException {
        super(*/
/*dockerImage,*//*
 memoryLimit, cpuShare);

        this.scalingFactor = scalingFactor;
        this.averageDeviceCount = averageDeviceCount;
    }
}
*/
