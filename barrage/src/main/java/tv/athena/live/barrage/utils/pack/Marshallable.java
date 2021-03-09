package tv.athena.live.barrage.utils.pack;


/**
 * marshal interface, marshal to pack and unmarshal to unpack
 * 
 * @author Vincent 2013-4-16
 */
public interface Marshallable {
	
	public void marshall(Pack pack);
	public void unmarshall(Unpack up);

}
