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
public class StartingAddressInvalidException extends ModbusException
{
  public StartingAddressInvalidException()
  {
  }

  public StartingAddressInvalidException( String s )
  {
    super( s );
  }
}


