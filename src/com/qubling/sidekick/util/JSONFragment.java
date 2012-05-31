/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick.util;

/**
 * Used with {@link StringTemplate} to allow for a complete JSON object or
 * array to be filled into a template.
 *
 * @author sterling
 *
 */
public interface JSONFragment {
    public String toJSONString();
}
