package tv.athena.live.barrage.utils.pack;


import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * pack the data to ByteBuffer
 * 
 * @author Vincent 2013-4-16
 */
public class Pack
{

	private ByteBuffer buffer;

	public Pack()
	{
		buffer = ByteBuffer.allocate(512);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
	}

	private void autoExpand(int expectedRemaining)
	{
		expand(expectedRemaining, true);
	}

	private void expand(int expectedRemaining, boolean autoExpand)
	{
		expand(buffer.position(), expectedRemaining, autoExpand);
	}

	private void expand(int pos, int expectedRemaining, boolean autoExpand)
	{
		int end = pos + expectedRemaining;
		int newCapacity;
		if (autoExpand)
		{
			newCapacity = normalizeCapacity(end);
		}
		else
		{
			newCapacity = end;
		}
		if (newCapacity > buffer.capacity())
		{
			capacity(newCapacity);
		}

		if (end > buffer.limit())
		{
			buffer.limit(end);
		}
	}

	public void capacity(int newCapacity)
	{

		if (newCapacity > buffer.capacity())
		{
			int pos = buffer.position();
			int limit = buffer.limit();
			ByteOrder bo = buffer.order();

			ByteBuffer oldBuf = buffer;
			ByteBuffer newBuf = ByteBuffer.allocate(newCapacity);
			oldBuf.clear();
			newBuf.put(oldBuf);
			buffer = newBuf;

			buffer.limit(limit);
			buffer.position(pos);
			buffer.order(bo);
		}
	}

	protected static int normalizeCapacity(int requestedCapacity)
	{
		if (requestedCapacity < 0)
		{
			return Integer.MAX_VALUE;
		}

		int newCapacity = Integer.highestOneBit(requestedCapacity);
		newCapacity <<= (newCapacity < requestedCapacity ? 1 : 0);
		return newCapacity < 0 ? Integer.MAX_VALUE : newCapacity;
	}

	public void replaceUint32(int pos, Uint32 val)
	{
		int now = buffer.position();
		buffer.position(pos);
		buffer.putInt(val.toInt()).position(now);
	}

	public void replaceUint16(int pos, Uint16 val)
	{
		int now = buffer.position();
		buffer.position(pos);
		buffer.putShort(val.toShort()).position(now);
	}

	public int size()
	{
		return buffer.position();
	}

	public ByteBuffer getBuffer()
	{
		return buffer;
	}
	
	public byte[] toBytes()
	{
		return buffer.array();
	}

	public Pack push(Uint32 val)
	{
		if (val == null)
		{
			throw new PackException("Uint32 is null");
		}
		autoExpand(4);
		buffer.putInt(val.toInt());
		return this;
	}

	public Pack push(Uint16 val)
	{
		if (val == null)
		{
			throw new PackException("Uint16 is null");
		}
		autoExpand(2);
		buffer.putShort(val.toShort());
		return this;
	}

	public Pack push(Uint64 val)
	{
		if (val == null)
		{
			throw new PackException("Uint64 is null");
		}
		autoExpand(8);
		buffer.putLong(val.toLong());
		return this;
	}

	public Pack push(Uint8 val)
	{
		autoExpand(1);
		buffer.put(val.toByte());
		return this;
	}

	public Pack push(byte b)
	{
		autoExpand(1);
		buffer.put(b);
		return this;
	}

	public Pack push(boolean val)
	{
		autoExpand(1);
		buffer.put((byte) (val ? 1 : 0));
		return this;
	}

	public Pack push(byte[] bytes)
	{
		autoExpand(2 + bytes.length);
		push(new Uint16(bytes.length));
		buffer.put(bytes);
		return this;
	}

	public Pack pushString32(byte[] bytes)
	{
		autoExpand(4 + bytes.length);
		push(new Uint32(bytes.length));
		buffer.put(bytes);
		return this;
	}

	/**
	 * fill pack with raw data which already packed
	 * @param data
	 * @return
	 */
	public Pack setRawData(byte[] data) {
		autoExpand(data.length);
		buffer.put(data);
		return this;
	}

	public Pack push(String str)
	{
		try
		{
			if (str == null)
				str = "";
			byte[] dst = str.getBytes("utf-8");
			if (dst.length > 0xFFFF)
			{
				throw new PackException("String too big");
			}
			return push(dst);
		}
		catch (UnsupportedEncodingException codeEx)
		{
			throw new PackException(codeEx);
		}
	}
	
	public Pack push(String str, String encoding)
	{
		try
		{
			if (str == null)
				str = "";
			byte[] dst = str.getBytes(encoding);
			if (dst.length > 0xFFFF)
			{
				throw new PackException("String too big");
			}
			return push(dst);
		}
		catch (UnsupportedEncodingException codeEx)
		{
			throw new PackException(codeEx);
		}
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
		byte[] b = new byte[buffer.limit()];
		buffer.get(b);
		buffer.flip();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < b.length; i++)
		{
			sb.append(Integer.toHexString(b[i] & 0xFF).toUpperCase()).append(" ");
		}
		return sb.toString();
	}
}
