package tv.athena.live.barrage.utils.pack;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * request to receive the network data
 * 
 * @author Vincent 2013-4-16
 */
public class Request extends Header
{

	private Unpack up;
	private ByteBuffer buffer;

	public Request(byte[] bytes) {
        reload(bytes);
	}

	public Request(byte[] bytes, int offset, int length) {
		reload(bytes, offset, length);
	}

    public void reload(byte[] bytes) {
        reload(bytes, 0, bytes.length);
    }

	/**
	 * reload and reuse this object
	 * @param bytes
	 * @param offset
	 * @param length
	 */
	public void reload(byte[] bytes, int offset, int length) {
		buffer = ByteBuffer.wrap(bytes, offset, length);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		if (up == null) {
			up = new Unpack(bytes, offset, length);
		} else {
			up.reload(bytes, offset, length);
		}
	}

	public static int peeklen(ByteBuffer buf)
	{
		if (buf.remaining() > 4)
		{
			buf.order(ByteOrder.LITTLE_ENDIAN);
			int oldPosition = buf.position();
			int packSize = buf.getInt();
			buf.position(oldPosition);
			return packSize;
		}
		return 0;
	}

	public void head() {
		this.length = up.popUint32();
		this.uri = up.popUint32();
		this.resCode = up.popUint16();
		// log.debug("Header[length={}, uri={}({}|{}), rescode={}]", new
		// Object[]{this.length.toInt(), this.uri.toInt(), this.getUriPrefix(),
		// this.getUriSuffix(), this.resCode.toInt()});
	}

	public Unpack getPackData()
	{
		return up;
	}
}
