package org.openimis.imispolicies.domain.entity;

public abstract class Report {

    private Report() {
    }

    abstract public int getSent();

    abstract public int getAccepted();

    public static class Feedback extends Report {
        private final int feedbackSent;
        private final int feedbackAccepted;

        public Feedback(
                int feedbackSent,
                int feedbackAccepted
        ) {
            this.feedbackSent = feedbackSent;
            this.feedbackAccepted = feedbackAccepted;
        }

        @Override
        public int getSent() {
            return feedbackSent;
        }

        @Override
        public int getAccepted() {
            return feedbackAccepted;
        }
    }

    public static class Renewal extends Report {
        private final int renewalSent;
        private final int renewalAccepted;

        public Renewal(
                int renewalSent,
                int renewalAccepted
        ) {
            this.renewalSent = renewalSent;
            this.renewalAccepted = renewalAccepted;
        }

        @Override
        public int getSent() {
            return renewalSent;
        }

        @Override
        public int getAccepted() {
            return renewalAccepted;
        }
    }

    public static class Enrolment {
        private final int enrolmentSubmitted;
        private final int enrolmentAssigned;

        public Enrolment(
                int enrolmentSubmitted,
                int enrolmentAssigned
        ) {
            this.enrolmentSubmitted = enrolmentSubmitted;
            this.enrolmentAssigned = enrolmentAssigned;
        }

        public int getSubmitted() {
            return enrolmentSubmitted;
        }

        public int getAssigned() {
            return enrolmentAssigned;
        }
    }
}
