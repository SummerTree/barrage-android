package tv.athena.live.barrage.utils.pack;


import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * pack ByteBuffer to data
 * 
 * @author Vincent 2013-4-16
 */
public class Unpack
{

	private ByteBuffer buffer;

	public Unpack(byte[] bytes)
	{
		this(bytes, 0, bytes.length);
	}

	public Unpack(byte[] bytes, int offset, int length)
	{
		reload(bytes, offset, length);
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
	}

	public int size()
	{
		return buffer.remaining();
	}

	public Uint32 popUint32()
	{
		return new Uint32(buffer.getInt());
	}

	public int popInt() {
		return buffer.getInt();
	}

	public Uint8 popUint8()
	{
		return new Uint8(buffer.get());
	}

	public Uint16 popUint16()
	{
		return new Uint16(buffer.getShort());
	}

	public Uint64 popUint64()
	{
		return new Uint64(buffer.getLong());
	}

	public boolean popBoolean()
	{
		return buffer.get() == 1 ? true : false;
	}

	public byte[] popBytes()
	{
		short size = buffer.getShort();
		byte[] dst = new byte[size];
		buffer.get(dst);
		return dst;
	}

	public String popString()
	{
		try
		{
			byte[] dst = popBytes();
			return new String(dst, "utf-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new UnpackException();
		}
	}
	
	public String popString(String encoding)
	{
		try
		{
			byte[] dst = popBytes();
			return new String(dst, encoding);
		}
		catch (UnsupportedEncodingException e)
		{
			throw new UnpackException();
		}
	}

	public String popString32()
	{
		try
		{
			int size = buffer.getInt();
			byte[] dst = new byte[size];
			buffer.get(dst);
			return new String(dst, "utf-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new UnpackException();
		}
	}
	
	public byte[] popBytes32()
	{
		int size = buffer.getInt();
		byte[] dst = new byte[size];
		buffer.get(dst);
		return dst;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Pack [buffer=");
		builder.append(bufferString());
		builder.append("]");
		return builder.toString();
	}

	private String bufferString()
	{
		byte[] b = remainBuffer();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < b.length; i++)
		{
			sb.append(Integer.toHexString(b[i] & 0xFF).toUpperCase()).append(" ");
		}
		return sb.toString();
	}

	/**
	 * remain buffer not pop yet
	 * @return remain data in byte array
	 */
	public byte[] remainBuffer() {
		byte[] b = new byte[buffer.remaining()];
		int oldPosition = buffer.position();
		buffer.get(b);
		buffer.position(oldPosition);
		return b;
	}
}
