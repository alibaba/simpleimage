package com.alibaba.security.simpleimage.analyze.harris;

public class Corner {
	int x, y;

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public float getH() {
		return h;
	}

	public void setH(float h) {
		this.h = h;
	}

	float h;

	public Corner(int x, int y, float h) {
		this.x = x;
		this.y = y;
		this.h = h;
	}
}
