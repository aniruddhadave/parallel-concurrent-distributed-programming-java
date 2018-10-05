package edu.coursera.concurrent;

import edu.rice.pcdp.Actor;
import edu.rice.pcdp.PCDP;

/**
 * An actor-based implementation of the Sieve of Eratosthenes.
 *
 * Fill in the empty SieveActorActor actor class below and use it from
 * countPrimes to determine the number of primes <= limit.
 */
public final class SieveActor extends Sieve {
    /**
     * {@inheritDoc}
     *
     * Use the SieveActorActor class to calculate the number of primes <=
     * limit in parallel. You might consider how you can model the Sieve of
     * Eratosthenes as a pipeline of actors, each corresponding to a single
     * prime number.
     */
    @Override
    public int countPrimes(final int limit) {
        final SieveActorActor sieveActor = new SieveActorActor(2);
        PCDP.finish(() -> {
        	for (int i=3; i<= limit; i++) {
        		sieveActor.send(i);
        	}
        	sieveActor.send(0);
        });
        
        int primeCount = 0;
        SieveActorActor loopActor = sieveActor;
        while(loopActor != null) {
        	primeCount += loopActor.localPrimesCount;
        	loopActor = loopActor.nextActor;
        }
        return primeCount;
    }

    /**
     * An actor class that helps implement the Sieve of Eratosthenes in
     * parallel.
     */
    public static final class SieveActorActor extends Actor {
        /**
         * Process a single message sent to this actor.
         *
         * @param msg Received message
         */
    	static final int MAX_LOCAL_PRIMES = 1000;
    	SieveActorActor nextActor;
    	int[] listPrimes;
    	int localPrimesCount;
    	
    	/**
    	 * @param localPrime
    	 */
    	public SieveActorActor(int localPrime) {
			this.listPrimes = new int[MAX_LOCAL_PRIMES];
			this.listPrimes[0] = localPrime;
			this.localPrimesCount = 1;
			this.nextActor = null;
		}
    	
        @Override
        public void process(final Object msg) {
            final int candidate = (Integer) msg;
            if (candidate <= 0) {
            	if (nextActor != null) {
            		nextActor.send(msg);
            	}
            	return;
            } else {
            	final boolean locallyPrime = isLocallyPrime(candidate);
            	if (locallyPrime) {
            		if (localPrimesCount < MAX_LOCAL_PRIMES) {
            			listPrimes[localPrimesCount] = candidate;
            			localPrimesCount += 1;
            		} else if (nextActor == null) {
            			nextActor = new SieveActorActor(candidate);
            		} else {
            			nextActor.send(msg);
            		}
            	}
            }
            	
        }
        
        private boolean isLocallyPrime(final int candidate) {
        	for (int i=0; i < localPrimesCount; i++) {
        		if (candidate % listPrimes[i] == 0) {
        			return false;
        		}
        	}
        	return true;
        }
    }
}
