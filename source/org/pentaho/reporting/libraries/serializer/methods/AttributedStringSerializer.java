/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2006 - 2009 Object Refinery Limited, Pentaho Corporation and Contributors.  All rights reserved.
 */

package org.pentaho.reporting.libraries.serializer.methods;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.text.AttributedString;
import java.util.Map;
import java.util.HashMap;

import org.pentaho.reporting.libraries.serializer.SerializeMethod;

/**
 * A serializer-method to serialize and deserialize attributed-strings.
 *
 * @author Thomas Morgner
 */
public class AttributedStringSerializer implements SerializeMethod
{
  /**
   * Default constructor.
   */
  public AttributedStringSerializer()
  {
  }

  /**
   * Writes a serializable object description to the given object output
   * stream.
   *
   * @param o   the to be serialized object.
   * @param stream the outputstream that should receive the object.
   * @throws IOException if an I/O error occured.
   */
  public void writeObject(Object o, ObjectOutputStream stream) throws IOException
  {
    AttributedString as = (AttributedString) o;
    AttributedCharacterIterator aci = as.getIterator();
    // build a plain string from aci
    // then write the string
    StringBuffer plainStr = new StringBuffer(100);
    char current = aci.first();
    while (current != CharacterIterator.DONE) {
        plainStr = plainStr.append(current);
        current = aci.next();
    }
    stream.writeObject(plainStr.toString());

    // then write the attributes and limits for each run
    current = aci.first();
    int begin = aci.getBeginIndex();
    while (current != CharacterIterator.DONE) {
        // write the current character - when the reader sees that this
        // is not CharacterIterator.DONE, it will know to read the
        // run limits and attributes
        stream.writeChar(current);

        // now write the limit, adjusted as if beginIndex is zero
        int limit = aci.getRunLimit();
        stream.writeInt(limit - begin);

        // now write the attribute set
        Map atts = new HashMap(aci.getAttributes());
        stream.writeObject(atts);
        current = aci.setIndex(limit);
    }
    // write a character that signals to the reader that all runs
    // are done...
    stream.writeChar(CharacterIterator.DONE);

  }

  /**
   * Reads the object from the object input stream.
   *
   * @param stream the object input stream from where to read the serialized data.
   * @return the generated object.
   * @throws IOException            if reading the stream failed.
   * @throws ClassNotFoundException if serialized object class cannot be found.
   */
  public Object readObject(ObjectInputStream stream)
          throws IOException, ClassNotFoundException
  {
    // read string and attributes then create result
    String plainStr = (String) stream.readObject();
    AttributedString result = new AttributedString(plainStr);
    char c = stream.readChar();
    int start = 0;
    while (c != CharacterIterator.DONE) {
        int limit = stream.readInt();
        Map atts = (Map) stream.readObject();
        result.addAttributes(atts, start, limit);
        start = limit;
        c = stream.readChar();
    }
    return result;
  }

  /**
   * The class of the object, which this object can serialize.
   *
   * @return the class of the object type, which this method handles.
   */
  public Class getObjectClass()
  {
    return AttributedString.class;
  }
}
