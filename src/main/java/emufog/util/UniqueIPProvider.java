package emufog.util;

import emufog.settings.Settings;

/**
 * The UniqueIPProvider calculates IP address within the subnet
 * address space defined in the base address of the given settings.
 */
public class UniqueIPProvider {

    /* the last assigned IP in the network */
    private String lastIP;

    private static UniqueIPProvider INSTANCE;

    /**
     * Creates a new IP provider with the base address of the
     * subnet from the given settings.
     *
     * @param settings settings for the subnet
     */
    UniqueIPProvider(Settings settings) {
        lastIP = settings.getBaseAddress();
    }


    /**
     * Returns the UniqueIPProvider instance. If not yet instantiated creates new instance.
     * @return singleton instance of UniqueIPProvider
     */
    public static UniqueIPProvider getInstance(){
        if(INSTANCE == null){
            try {
                INSTANCE = new UniqueIPProvider(Settings.getSettings());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return INSTANCE;
    }

    /**
     * Calculates and returns the next available IP address in the subnet.
     *
     * @return IP address
     */
    public String getNextIPV4Address() {
        String[] nums = lastIP.split("\\.");
        int i = (Integer.parseInt(nums[0]) << 24 | Integer.parseInt(nums[2]) << 8
                | Integer.parseInt(nums[1]) << 16 | Integer.parseInt(nums[3])) + 1;

        // If you wish to skip over .255 addresses.
        if ((byte) i == -1) {
            i++;
        }

        lastIP = String.format("%d.%d.%d.%d", i >>> 24 & 0xFF, i >> 16 & 0xFF,
                i >> 8 & 0xFF, i >> 0 & 0xFF);

        return lastIP;
    }
}
