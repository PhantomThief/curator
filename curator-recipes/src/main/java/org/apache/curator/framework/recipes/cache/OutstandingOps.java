/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.curator.framework.recipes.cache;

import java.util.concurrent.atomic.AtomicLong;

class OutstandingOps
{
    private final Runnable completionProc;
    private volatile AtomicLong count = new AtomicLong(0);

    OutstandingOps(Runnable completionProc)
    {
        this.completionProc = completionProc;
    }

    void increment()
    {
        AtomicLong localCount = count;
        if ( localCount != null )
        {
            localCount.incrementAndGet();
        }
    }

    void decrement()
    {
        AtomicLong localCount = count;
        if ( localCount != null )
        {
            if ( (localCount.decrementAndGet() == 0) )
            {
                count = null;
                if ( localCount.compareAndSet(0, Long.MIN_VALUE) )
                {
                    // use Long.MIN_VALUE as a sentinel to avoid any races with the count.
                    // Only 1 thread will successfully set count to Long.MIN_VALUE and
                    // thus completionProc will only get called once
                    completionProc.run();
                }
            }
        }
    }
}
