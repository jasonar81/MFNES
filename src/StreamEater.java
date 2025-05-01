//Handle java silliness

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class StreamEater extends Thread
  {
  BufferedReader br;

  /** Construct a StreamEater on an InputStream. */
  public StreamEater (InputStream is)
    {
    this.br = new BufferedReader (new InputStreamReader (is));
    }

  public void run ()
    {
    try
      {
      String line;
      while ((line = br.readLine()) != null)
        {
	// Process the line of output in some way
        }
      }
    catch (IOException e)
      {
      // Do something to handle exception
      }
    finally
      {
      try { br.close(); } catch (Exception e) {};
      }
    }
  }