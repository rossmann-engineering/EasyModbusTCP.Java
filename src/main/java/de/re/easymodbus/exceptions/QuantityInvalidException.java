/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.re.easymodbus.exceptions;

/**
 *
 * @author Stefan Roßmann
 */
@SuppressWarnings("serial")
public class QuantityInvalidException extends ModbusException
{
  public QuantityInvalidException()
  {
  }

  public QuantityInvalidException( String s )
  {
    super( s );
  }
}


