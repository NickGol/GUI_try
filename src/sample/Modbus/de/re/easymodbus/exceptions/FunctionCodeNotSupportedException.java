/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sample.Modbus.de.re.easymodbus.exceptions;

/**
 *
 * @author Stefan Roßmann
 */
@SuppressWarnings("serial")
public class FunctionCodeNotSupportedException extends ModbusException
{
  public FunctionCodeNotSupportedException()
  {
  }

  public FunctionCodeNotSupportedException( String s )
  {
    super( s );
  }
}


