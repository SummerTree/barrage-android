package tv.athena.live.barrage.utils.pack;


/**
 * yy protocol header
 * 
 * @author Vincent 2013-4-16
 */
public class Header
{
	public final static Uint16 RES_SUCCESS = new Uint16(200);
	protected Uint32 uri = Uint32.toUInt(0);
	protected Uint32 length = Uint32.toUInt(0);
	protected Uint16 resCode = Uint16.toUInt(200);

	public Uint32 getUri()
	{
		return uri;
	}

	public void setUri(Uint32 uri)
	{
		this.uri = uri;
	}

	public Uint32 getLength()
	{
		return length;
	}

	public void setLength(Uint32 length)
	{
		this.length = length;
	}

	public Uint16 getResCode()
	{
		return resCode;
	}

	public void setResCode(Uint16 resCode)
	{
		this.resCode = resCode;
	}
	
	public boolean isSuccess()
	{
		return resCode.toLong() == 200L;
	}
	
	public int getUriPrefix()
	{
		return uri.toInt() >> 8;
	}
	
	public int getUriSuffix()
	{
		return uri.toInt() - (uri.toInt() >> 8 << 8);
	}
}
