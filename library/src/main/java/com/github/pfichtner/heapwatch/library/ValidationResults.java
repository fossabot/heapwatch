package com.github.pfichtner.heapwatch.library;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.function.Predicate;

import org.hamcrest.Description;
import org.hamcrest.StringDescription;

import com.github.pfichtner.heapwatch.library.acl.Stats;

public final class ValidationResults {

	private static class ValidationSuccess implements ValidationResult {

		private String message;

		public ValidationSuccess(FunctionBasedMatcher<?> matcher, Stats stats) {
			this.message = text(matcher, stats);
		}

		private static String text(FunctionBasedMatcher<?> matcher, Stats stats) {
			return new StringDescription() //
					.appendDescriptionOf(matcher) //
					.appendText(" matched for value ") //
					.appendValue(matcher.function.apply(stats)) //
					.toString();
		}

		public boolean isError() {
			return false;
		}

		public String getMessage() {
			return this.message;
		}

	}

	private static class ValidationError implements ValidationResult {

		private final String errorMessage;

		public ValidationError(FunctionBasedMatcher<?> matcher, Stats stats) {
			this.errorMessage = text(matcher, stats);
		}

		private static String text(FunctionBasedMatcher<?> matcher, Stats stats) {
			Description description = new StringDescription().appendText("Expected ").appendDescriptionOf(matcher)
					.appendText(" but ");
			matcher.matcher.describeMismatch(matcher.function.apply(stats), description);
			return description.toString();
		}

		public boolean isError() {
			return true;
		}

		public String getMessage() {
			return this.errorMessage;
		}

	}

	private ValidationResults() {
		super();
	}

	public static ValidationResult ok(FunctionBasedMatcher<?> matcher, Stats stats) {
		return new ValidationSuccess(matcher, stats);
	}

	public static ValidationResult error(FunctionBasedMatcher<?> matcher, Stats stats) {
		return new ValidationError(matcher, stats);
	}

	public static List<ValidationResult> errors(List<ValidationResult> validations) {
		return filter(validations, ValidationResult::isError);
	}

	public static List<ValidationResult> oks(List<ValidationResult> validations) {
		return filter(validations, not(ValidationResult::isError));
	}

	private static <T> Predicate<T> not(Predicate<T> predicate) {
		return predicate.negate();
	}

	private static List<ValidationResult> filter(List<ValidationResult> validations,
			Predicate<? super ValidationResult> predicate) {
		return validations.stream().filter(predicate).collect(toList());
	}

}
