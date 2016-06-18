package gyurix.particle;

import gyurix.spigotutils.BlockData;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Created by GyuriX.
 */
public class ParticleAPI {
    /**
     * Shows a block or an iron crack particle of the given block data with offset=[0,0,0], data=0 and count=1 to the given players
     *
     * @param iron    - Should the showable particle be an iron crack or just a simple block crack?
     * @param bd      - The data of the block data of the showable particle
     * @param loc     - The location of the particle
     * @param players - The players who will see the particle (empty = every player)
     */
    public static void showCrack(boolean iron, BlockData bd, Location loc, Player... players) {

    }

    /**
     * Shows a block or an iron crack particle with the given parameters to the given players
     *
     * @param iron    - Should the showable particle be an iron crack or just a simple block crack?
     * @param bd      - The data of the block data of the showable particle
     * @param loc     - The location of the particle
     * @param offset  - The vector multiplied by nextGaussian
     * @param data    - The data of the particle
     * @param count   - The amount of the showable particles
     * @param players - The players who will see the particle (empty = every player)
     */
    public static void showCrack(boolean iron, BlockData bd, Location loc, Vector offset, float data, int count, Player... players) {

    }

    /**
     * Shows a block dust particle of the given blockId data with offset=[0,0,0], data=0 and count=1 to the given players
     *
     * @param iron    - Should the showable particle be an iron crack or just a simple block crack?
     * @param blockId - The id of the block data of the showable particle
     * @param loc     - The location of the particle
     * @param players - The players who will see the particle (empty = every player)
     */
    public static void showDust(boolean iron, int blockId, Location loc, Player... players) {

    }

    /**
     * Shows a block dust particle of the given blockId with the given parameters to the given players
     *
     * @param iron    - Should the showable particle be an iron crack or just a simple block crack?
     * @param blockId - The id of the block data of the showable particle
     * @param loc     - The location of the particle
     * @param offset  - The vector multiplied by nextGaussian
     * @param data    - The data of the particle
     * @param count   - The amount of the showable particles
     * @param players - The players who will see the particle (empty = every player)
     */
    public static void showDust(boolean iron, int blockId, Location loc, Vector offset, float data, int count, Player... players) {

    }

    /**
     * Shows the given particle with the given parameters to the given players
     *
     * @param particle - The particle
     * @param loc      - The location of the particle
     * @param offset   - The vector multiplied by nextGaussian
     * @param data     - The data of the particle
     * @param count    - The amount of the showable particles
     * @param players  - The players who will see the particle (empty = every player)
     */
    public static void showParticle(Particle particle, Location loc, Vector offset, float data, int count, Player... players) {
    }

    /**
     * Shows the given particle at the given location with offset=[0,0,0], data=0 and count=1 to the given players
     *
     * @param particle - The particle
     * @param loc      - The location of the particle
     * @param players  - The players who will see the particle (empty = every player)
     */
    public static void showParticle(Particle particle, Location loc, Player... players) {

    }


}
